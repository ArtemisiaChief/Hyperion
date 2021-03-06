package dedicatedserver.entity;

import java.util.concurrent.ConcurrentHashMap;

public class Class {

    private String id;

    private int checkInCount;

    private ConcurrentHashMap<String, Student> studentsInClass;

    public Class(String id, int checkInCount) {
        this.id = id;
        this.checkInCount = checkInCount;
        this.studentsInClass = new ConcurrentHashMap<>();
    }

    public Class(String id, int checkInCount, ConcurrentHashMap<String, Student> studentsInClass) {
        this.id = id;
        this.checkInCount = checkInCount;
        this.studentsInClass = studentsInClass;
    }

    public String getId() {
        return id;
    }

    public int getCheckInCount() {
        return checkInCount;
    }

    public ConcurrentHashMap<String, Student> getStudentsInClass() {
        return studentsInClass;
    }

    public void setCheckInCount(int checkInCount) {
        this.checkInCount = checkInCount;
    }

}