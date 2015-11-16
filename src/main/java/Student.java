public class Student {
    Student(int studentId, String username, String address) {
        this.studentId = studentId;
        this.username = username;
        this.address = address;
    }

    public String getAddress() {
        return address;
    }

    public String getName() {
        return username;
    }

    public int getStudentId() {
        return studentId;
    }

    private int studentId;
    private String username;
    private String address;
}
