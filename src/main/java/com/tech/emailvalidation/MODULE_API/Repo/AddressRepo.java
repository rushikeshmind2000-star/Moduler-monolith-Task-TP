package com.tech.emailvalidation.MODULE_API.Repo;

import com.tech.emailvalidation.MODULE_API.Model.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AddressRepo extends JpaRepository<Address, Integer> {
}
