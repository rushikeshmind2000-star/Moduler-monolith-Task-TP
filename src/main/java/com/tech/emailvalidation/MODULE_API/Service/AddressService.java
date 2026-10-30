package com.tech.emailvalidation.MODULE_API.Service;

import com.tech.emailvalidation.MODULE_API.GlobalException.ResourceNotFoundException;
import com.tech.emailvalidation.MODULE_API.Model.Address;
import com.tech.emailvalidation.MODULE_API.Repo.AddressRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AddressService {

    private final AddressRepo addressRepo;

    @Autowired
    public AddressService(AddressRepo addressRepo) {
        this.addressRepo = addressRepo;
    }

    public Address addAddress(Address address) {
        return addressRepo.save(address);
    }

    public List<Address> getAllAddresses() {
        return addressRepo.findAll();
    }

    public Address getAddressById(int addressId) {
        return addressRepo.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with ID: " + addressId));
    }

    public Address updateAddress(int addressId, Address updatedAddress) {
        Address existingAddress = getAddressById(addressId);
        if (updatedAddress.getCity() != null) {
            existingAddress.setCity(updatedAddress.getCity());
        }
        if (updatedAddress.getState() != null) {
            existingAddress.setState(updatedAddress.getState());
        }
        if (updatedAddress.getPincode() != null) {
            existingAddress.setPincode(updatedAddress.getPincode());
        }
        return addressRepo.save(existingAddress);
    }

    public String deleteAddressById(int addressId) {
        Address address = getAddressById(addressId);
        addressRepo.delete(address);
        return "Deleted Address with ID: " + addressId;
    }
}
