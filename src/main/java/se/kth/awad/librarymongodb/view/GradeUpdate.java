package se.kth.awad.librarymongodb.view;
//Datacontainer för att bära boktitel och betyg mellan dialog och controller.
public class GradeUpdate {
    private final String title;
    private final int grade;

    public GradeUpdate(String title, int grade) {
        this.title = title;
        this.grade = grade;
    }

    //Getters
    public String getTitle() {return title;}
    public int getGrade() {return grade;}
}
