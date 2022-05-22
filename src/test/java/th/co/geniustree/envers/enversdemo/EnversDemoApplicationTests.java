package th.co.geniustree.envers.enversdemo;

import org.hibernate.envers.AuditReaderFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import th.co.geniustree.envers.enversdemo.domain.Customer;
import th.co.geniustree.envers.enversdemo.domain.Product;
import th.co.geniustree.envers.enversdemo.domain.RequestForm;
import th.co.geniustree.envers.enversdemo.repo.RequestFormRepo;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED;

@SpringBootTest
@Transactional(propagation = NOT_SUPPORTED)
class EnversDemoApplicationTests {
    private static final Logger log = getLogger(EnversDemoApplicationTests.class);
    @Autowired
    RequestFormRepo repo;
    @Autowired
    EntityManager em;
    @Autowired
    private PlatformTransactionManager transactionManager;

    private TransactionTemplate transactionTemplate;

    @BeforeEach
    void setUp() {
        transactionTemplate = new TransactionTemplate(transactionManager);
    }


    @Test
    void contextLoads() {
        Product product = new Product(1, "A");
        Customer customer = new Customer(1, "Customer-A");
        RequestForm form = new RequestForm(1, "ECODE-1");
        form.getProducts().add(product);
        form.setCustomer(customer);
        //Start
        log.info("REV.1-----------------");
        transactionTemplate.execute(e -> {
            em.persist(form);
            em.flush();
            return form;
        });

        // Clear Entity context so All are detached now.
        em.clear();
        Assertions.assertTrue(!em.contains(form));

        // Update all relate entity
        log.info("REV.2-----------------");
        transactionTemplate.execute(e -> {
            form.setEcoding("ECODE-2");
            form.getCustomer().setName("Customer-B");
            form.getProducts().get(0).setName("B");
            em.merge(form);
            em.flush();
            return form;
        });

        //Update only customer. So only CUSTOMER_AUD has REV.3 other *AUD table just skip to next REV
        //Revision are global,So it increases for every change entity
        log.info("REV.3-----------------");
        transactionTemplate.execute(e -> {
            Customer c = em.find(Customer.class,1);
            c.setName("Customer-C");
            em.flush();
            return c;
        });

        //REV.4 all *AUD table has REV.4
        log.info("REV.4-----------------");
        transactionTemplate.execute(e -> {
            form.setEcoding("ECODE-3");
            form.getCustomer().setName("Customer-D");
            form.getProducts().get(0).setName("D");
            em.merge(form);
            em.flush();
            return form;
        });



        transactionTemplate.execute(e -> {
            RequestForm revForm = (RequestForm) AuditReaderFactory.get(em).createQuery().forEntitiesAtRevision(RequestForm.class, 1).getSingleResult();
            Assertions.assertEquals( "Customer-A",revForm.getCustomer().getName());
            Assertions.assertEquals( "A",revForm.getProducts().get(0).getName());
            return form;
        });

        transactionTemplate.execute(e -> {
            RequestForm revForm = (RequestForm) AuditReaderFactory.get(em).createQuery().forEntitiesAtRevision(RequestForm.class, 2).getSingleResult();
            Assertions.assertEquals( "Customer-B",revForm.getCustomer().getName());
            Assertions.assertEquals( "B",revForm.getProducts().get(0).getName());
            return form;
        });

        transactionTemplate.execute(e -> {
            RequestForm revForm = (RequestForm) AuditReaderFactory.get(em).createQuery().forEntitiesAtRevision(RequestForm.class, 4).getSingleResult();
            Assertions.assertEquals( "Customer-D",revForm.getCustomer().getName());
            Assertions.assertEquals( "D",revForm.getProducts().get(0).getName());
            return form;
        });

    }

}
