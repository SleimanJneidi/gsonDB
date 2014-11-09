package gsonDB;

/**
 *
 * Created by Sleiman on 30/09/2014.
 */

import java.util.ArrayList;
import java.util.List;

public class Person {
    private String firstName;
    private String surName;
    private int age;
    private Gender gender;
    private String eMail;
    private String phone;
    private String address;

    public static class Builder{

        private String firstName;
        private String surName;
        private int age;
        private Gender gender;
        private String eMail;
        private String phone;
        private String address;

        public Person.Builder firstName(String givenName){
            this.firstName = givenName;
            return this;
        }

        public Person.Builder surName(String surName){
            this.surName = surName;
            return this;
        }

        public Person.Builder age (int val){
            age = val;
            return this;
        }

        public Person.Builder gender(Gender val){
            gender = val;
            return this;
        }

        public Person.Builder email(String val){
            eMail = val;
            return this;
        }

        public Person.Builder phoneNumber(String val){
            phone = val;
            return this;
        }

        public Person.Builder address(String val){
            address = val;
            return this;
        }

        public Person build(){
            return new Person(this);
        }
    }

    private Person(){

    }

    private Person(Person.Builder builder){
        firstName = builder.firstName;
        surName = builder.surName;
        age = builder.age;
        gender = builder.gender;
        eMail = builder.eMail;
        phone = builder.phone;
        address = builder.address;

    }

    public String getFirstName(){
        return firstName;
    }

    public String getSurName(){
        return surName;
    }

    public int getAge(){
        return age;
    }

    public Gender getGender(){
        return gender;
    }

    public String getEmail(){
        return eMail;
    }

    public String getPhone(){
        return phone;
    }

    public String getAddress(){
        return address;
    }

    public void print(){
        System.out.println(
                "\nName: " + firstName + " " + surName + "\n" +
                        "Age: " + age + "\n" +
                        "Gender: " + gender + "\n" +
                        "eMail: " + eMail + "\n" +
                        "Phone: " + phone + "\n" +
                        "Address: " + address + "\n"
        );
    }

    @Override
    public String toString(){
        return "Name: " + firstName + " " + surName + "\n" + "Age: " + age + "  Gender: " + gender + "\n" + "eMail: " + eMail + "\n";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Person person = (Person) o;

        if (age != person.age) return false;
        if (address != null ? !address.equals(person.address) : person.address != null) return false;
        if (eMail != null ? !eMail.equals(person.eMail) : person.eMail != null) return false;
        if (firstName != null ? !firstName.equals(person.firstName) : person.firstName != null) return false;
        if (gender != person.gender) return false;
        if (phone != null ? !phone.equals(person.phone) : person.phone != null) return false;
        if (surName != null ? !surName.equals(person.surName) : person.surName != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = firstName != null ? firstName.hashCode() : 0;
        result = 31 * result + (surName != null ? surName.hashCode() : 0);
        result = 31 * result + age;
        result = 31 * result + (gender != null ? gender.hashCode() : 0);
        result = 31 * result + (eMail != null ? eMail.hashCode() : 0);
        result = 31 * result + (phone != null ? phone.hashCode() : 0);
        result = 31 * result + (address != null ? address.hashCode() : 0);
        return result;
    }

    public static List<Person> createShortList(){
        List<Person> people = new ArrayList<>();

        people.add(
                new Person.Builder()
                        .firstName("Bob")
                        .surName("Baker")
                        .age(21)
                        .gender(Gender.MALE)
                        .email("bob.baker@example.com")
                        .phoneNumber("201-121-4678")
                        .address("44 4th St, Smallville, KS 12333")
                        .build()
        );

        people.add(
                new Person.Builder()
                        .firstName("Jane")
                        .surName("Doe")
                        .age(25)
                        .gender(Gender.FEMALE)
                        .email("jane.doe@example.com")
                        .phoneNumber("202-123-4678")
                        .address("33 3rd St, Smallville, KS 12333")
                        .build()
        );

        people.add(
                new Person.Builder()
                        .firstName("John")
                        .surName("Doe")
                        .age(25)
                        .gender(Gender.MALE)
                        .email("john.doe@example.com")
                        .phoneNumber("202-123-4678")
                        .address("33 3rd St, Smallville, KS 12333")
                        .build()
        );

        people.add(
                new Person.Builder()
                        .firstName("James")
                        .surName("Johnson")
                        .age(45)
                        .gender(Gender.MALE)
                        .email("james.johnson@example.com")
                        .phoneNumber("333-456-1233")
                        .address("201 2nd St, New York, NY 12111")
                        .build()
        );

        people.add(
                new Person.Builder()
                        .firstName("Joe")
                        .surName("Bailey")
                        .age(67)
                        .gender(Gender.MALE)
                        .email("joebob.bailey@example.com")
                        .phoneNumber("112-111-1111")
                        .address("111 1st St, Town, CA 11111")
                        .build()
        );

        people.add(
                new Person.Builder()
                        .firstName("Phil")
                        .surName("Smith")
                        .age(55)
                        .gender(Gender.MALE)
                        .email("phil.smith@examp;e.com")
                        .phoneNumber("222-33-1234")
                        .address("22 2nd St, New Park, CO 222333")
                        .build()
        );

        people.add(
                new Person.Builder()
                        .firstName("Betty")
                        .surName("Jones")
                        .age(85)
                        .gender(Gender.FEMALE)
                        .email("betty.jones@example.com")
                        .phoneNumber("211-33-1234")
                        .address("22 4th St, New Park, CO 222333")
                        .build()
        );


        return people;
    }

}

