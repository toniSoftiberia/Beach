package cat.lafosca.platja.beach;

// Container Lost Kids
public class LostKid {

    private String name;
    private int age;

    LostKid(String pName,int pAge) {
        setAge(pAge);
        setName(pName);
    }

    public void setAge(int age) {
        this.age = age;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public String getName() {
        return name;
    }
}