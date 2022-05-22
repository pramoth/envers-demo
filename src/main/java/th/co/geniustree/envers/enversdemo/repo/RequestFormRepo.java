package th.co.geniustree.envers.enversdemo.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import th.co.geniustree.envers.enversdemo.domain.RequestForm;

public interface RequestFormRepo extends JpaRepository<RequestForm,Integer> {
}
