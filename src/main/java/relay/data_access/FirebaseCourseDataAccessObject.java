
package relay.data_access;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.apache.velocity.exception.ResourceNotFoundException;
import relay.entity.Course;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;




public class FirebaseCourseDataAccessObject {
    private final Firestore db;

    public FirebaseCourseDataAccessObject() {
        this.db = FirestoreSingleton.get();
    }

    /**
     * Creates a new course document in Firestore and sets the courseID within the provided Course object.
     *
     * @param course The Course object containing course details to be added to Firestore.
     * @throws RuntimeException If an InterruptedException or ExecutionException occurs during Firestore operations.
     */
    public void save(Course course) {
        Map<String, Object> courseDocument = new HashMap<>();
        courseDocument.put("courseName", course.getCourseName());
        courseDocument.put("instructorID", course.getInstructor().getInstructorID());

        ApiFuture<DocumentReference> newCourseDocument = db.collection("courses").add(courseDocument);
        try {
            String courseID = newCourseDocument.get().getId();
            course.setCourseID(courseID);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * Retrieves a list of courses associated with a specific instructor ID.
     *
     * @param instructorID The ID of the instructor whose courses are to be retrieved.
     * @return An ArrayList of Course objects associated with the specified instructor ID.
     * @throws NullPointerException if instructorID is null.
     */
    public ArrayList<Course> getCoursesByInstructor(String instructorID) {
        if (instructorID == null){
            throw new NullPointerException();
        }

        Query query = db.collection("courses")
                .whereEqualTo("instructorID", instructorID);

        ApiFuture<QuerySnapshot> querySnapshot = query.get();

        ArrayList<Course> courses = new ArrayList<>();
        try {
            for (DocumentSnapshot document : querySnapshot.get().getDocuments()) {

                Map<String, Object> courseData = document.getData();

                String courseID = document.getId();
                String courseName = (String) courseData.get("courseName");

                FirebaseInstructorDataAccessObject instructorDataAccessObject = new FirebaseInstructorDataAccessObject();

                Course course = new Course(courseName, instructorDataAccessObject.read(instructorID));
                course.setCourseID(courseID);
                courses.add(course);

            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return courses;
    }

    /**
     * Checks if a course with the given course ID exists in the Firestore database.
     *
     * @param courseID the ID of the course to check for existence
     * @return {@code true} if the course with the specified ID exists, {@code false} otherwise
     */
    public boolean exists(String courseID) {
        ApiFuture<DocumentSnapshot> retrievedcourseDocument = db.collection("courses").document(courseID).get();
        DocumentSnapshot courseDocument = null;
        try {
            courseDocument = retrievedcourseDocument.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return courseDocument.exists();
    }

    /**
     * Deletes a course from Firestore based on the provided course ID.
     *
     * @param courseID the ID of the course to be deleted
     * @throws ResourceNotFoundException if the course with the specified ID does not exist or if an error occurs during deletion
     */
    public void delete(String courseID) throws RuntimeException{
        try {
            if (!exists(courseID)) {
                throw new ResourceNotFoundException("Course with given ID does not exist");
            }

            ApiFuture<WriteResult> deleteResult = FirestoreSingleton.get().collection("courses").document(courseID).delete();
            deleteResult.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

}

