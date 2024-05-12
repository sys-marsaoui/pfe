package com.rgi.hiring.accountManagement.controllers;

import com.rgi.hiring.accountManagement.modals.PersonalCredentials;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.rgi.hiring.accountManagement.repository.PersonalCredentialsRepository;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
// Annotation
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
public class PersonalCredentialsController {

    @Autowired
    private PersonalCredentialsRepository PersonalCredentialsRepository;

    @PostMapping("/addAccount")
    public String saveBook(@RequestBody PersonalCredentials PersonalCredentials){
        PersonalCredentialsRepository.save(PersonalCredentials);

        return "Added Successfully";
    }

    @PutMapping("/editAccount/{id}")
    public ResponseEntity<?> update(@PathVariable("id") String id, @RequestBody PersonalCredentials sourceItem) {
        Optional<PersonalCredentials> optionalTargetItem = PersonalCredentialsRepository.findById(id);
        if (optionalTargetItem.isPresent()) {
            PersonalCredentials targetItem = optionalTargetItem.get();
//            // Make sure the ID matches
//            if (!id.equals(sourceItem.getId())) {
//                return ResponseEntity.badRequest().body("ID mismatch");
//            }
            try {
                // Iterate over sourceItem properties
                for (Field field : sourceItem.getClass().getDeclaredFields()) {
                    // Make the field accessible (in case it's private)
                    field.setAccessible(true);
                    // Get the value of the field from sourceItem
                    Object propertyValue = field.get(sourceItem);
                    // Get the setter method for the field in targetItem
                    String fieldName = field.getName();
                    String setterMethodName = "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
                    Method setterMethod = targetItem.getClass().getMethod(setterMethodName, field.getType());
                    // Update property in targetItem if not null and has a setter method
                    if (propertyValue != null && setterMethod != null) {
                        setterMethod.invoke(targetItem, propertyValue);
                    }
                }
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                // Handle reflection exceptions
                e.printStackTrace();
            }
            return ResponseEntity.ok(PersonalCredentialsRepository.save(targetItem));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/findAllAccount")
    public List<PersonalCredentials> getPersonalCredentials() {

        return PersonalCredentialsRepository.findAll();
    }

    @DeleteMapping("/delete/{id}")
    public String deleteBook(@PathVariable String id){
        PersonalCredentialsRepository.deleteById(id);

        return "Deleted Successfully";
    }
}
