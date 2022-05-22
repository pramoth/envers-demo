package th.co.geniustree.envers.enversdemo.domain;

import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Audited
@Table(name = "request_form")
public class RequestForm implements Serializable {
    @Id
    private int id;

    private String ecoding;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "request_form_id")
    private List<Product> products;

    @OneToOne(cascade = CascadeType.ALL)
    private Customer customer;

    public RequestForm() {
    }

    public RequestForm(int id, String ecoding) {
        this.id = id;
        this.ecoding = ecoding;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEcoding() {
        return ecoding;
    }

    public void setEcoding(String ecoding) {
        this.ecoding = ecoding;
    }

    public List<Product> getProducts() {
        if (products == null) {
            products = new ArrayList<>();
        }
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RequestForm that = (RequestForm) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
