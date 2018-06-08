import manage.AuthFITBIT;

public class Main {
    public static void main(String[] args) throws Exception {
        AuthFITBIT fitbit = new AuthFITBIT();

        fitbit.run("https://api.fitbit.com/1/user/-/activities/heart/date/today/1d.json");
    }
}
