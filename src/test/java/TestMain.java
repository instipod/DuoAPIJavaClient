import com.instipod.duoapi.*;
import com.instipod.duoapi.exceptions.DuoRequestFailedException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class TestMain {
    public static void main(String[] args) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        DuoAPIObject duo = new DuoAPIObject("api-79c277ee.duosecurity.com", "DIZ3LA5O2IUU7Z9RV24T", "fW8OjOHLlrnH38lfzfUPxqLqDEJDnGh1pSh5Mq9u", 10);
        duo.enableFineLogging();

        DuoUser user = duo.getUser("michael.kelly.2", "1.1.1.1");
        user.refresh();
        System.out.println("Should user enroll? " + user.shouldEnroll());
        System.out.println("Must user enroll? " + user.mustEnroll());
        System.out.println("Can user enroll here? " + user.canEnrollHere());
        if (user.canEnrollHere()) {
            System.out.println("Enroll link is " + user.getEnrollURL());
        }
        System.out.println("Auth default action is " + user.getDefaultAction());
        System.out.println("Device list: " + user.getDevices());

        System.out.println("");
        System.out.println("------- PUSH TESTING --------");
        System.out.println("");
        System.out.println("has push capability? " + user.hasCapability("push"));
        if (user.hasCapability("push")) {
            System.out.println("Attempting to send a push...");
            String[] prefs = new String[1];
            prefs[0] = "push";
            DuoPushCapableDevice pushDevice = (DuoPushCapableDevice) user.getFirstDevice(prefs);
            DuoDelayedTransaction transact = pushDevice.push(user.getUsername(), "My cool app", "1.1.1.1");
            if (transact == null) {
                System.out.println("The transaction was declined right away.  Possible issue?");
            } else {
                while (!transact.hasCompleted()) {
                    try {
                        transact.checkStatus();
                    } catch (Exception ex) {
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                    }
                }
                try {
                    System.out.println("The result of that transaction was " + transact.checkStatus());
                } catch (DuoRequestFailedException e) {
                    e.printStackTrace();
                }
            }
        }

        System.out.println("");
        System.out.println("------- MOBILE OTP TESTING --------");
        System.out.println("");
        System.out.println("has mobile_otp capability? " + user.hasCapability("mobile_otp"));
        if (user.hasCapability("mobile_otp")) {
            System.out.println("Sending the challenge...");
            String[] prefs = new String[1];
            prefs[0] = "mobile_otp";
            DuoCodeCapableDevice codeDevice = (DuoCodeCapableDevice) user.getFirstDevice(prefs);
            System.out.println("was challenge sent? " + codeDevice.challenge(user.getUsername(), "1.1.1.1"));
            try {
                System.out.print("Enter code: ");
                String code = reader.readLine();
                System.out.println("result was " + codeDevice.checkResponse(user.getUsername(), "1.1.1.1", code));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        System.out.println("");
        System.out.println("------- SMS TESTING --------");
        System.out.println("");
        System.out.println("has sms capability? " + user.hasCapability("sms"));
        if (user.hasCapability("sms")) {
            System.out.println("Sending the challenge...");
            String[] prefs = new String[1];
            prefs[0] = "sms";
            DuoCodeCapableDevice codeDevice = (DuoCodeCapableDevice) user.getFirstDevice(prefs);
            System.out.println("was challenge sent? " + codeDevice.challenge(user.getUsername(), "1.1.1.1"));
            try {
                System.out.print("Enter code: ");
                String code = reader.readLine();
                System.out.println("result was " + codeDevice.checkResponse(user.getUsername(), "1.1.1.1", code));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
