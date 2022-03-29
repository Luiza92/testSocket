
import java.util.regex.Pattern;

public class Validation {

    public boolean isValidPhoneNumber(String phoneNumber) {
        String ePattern = "^[+]*[(]{0,1}[0-9]{1,4}[)]{0,1}[\\s\\./0-9]*$";
        Pattern p = Pattern.compile(ePattern);
        java.util.regex.Matcher m = p.matcher(phoneNumber);
        return m.matches();
    }
    //+(374)77372299

    public boolean isValidAutoNumber(String autoNumber) {
        String ePattern = "^[0-9]{2}[A-Z]{2}[0-9]{3}$";
        Pattern p = Pattern.compile(ePattern);
        java.util.regex.Matcher m = p.matcher(autoNumber);
        return m.matches();
    }
    //12AA123

}
