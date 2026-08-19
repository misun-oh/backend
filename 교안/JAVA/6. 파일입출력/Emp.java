package ex09.file;

public class Emp {
    private String empId;
    private String empName;
    private int salary;

    public Emp(String empId, String empName, int salary) {
        this.empId = empId;
        this.empName = empName;
        this.salary = salary;
    }

    public String getEmpId() {
        return empId;
    }

    public String getEmpName() {
        return empName;
    }

    public int getSalary() {
        return salary;
    }
}
