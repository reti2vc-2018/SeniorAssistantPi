package manage;

public class OAuth2ClientCredentials {

        /** Value of the "API Key". */
        public static final String API_KEY = "22CSTL"; //maybe togliere le virgolette

        /** Value of the "API Secret". */
        public static final String API_SECRET = "ea2452013abd35609940ce5601960a08"; //maybe togliere le virgolette

        /** Port in the "Callback URL". */
        public static final int PORT = 8080;

        /** Domain name in the "Callback URL". */
        public static final String DOMAIN = "127.0.0.1";

        public static void errorIfNotSpecified() {
            if (API_KEY.startsWith("Enter ") || API_SECRET.startsWith("Enter ")) {
                System.out.println(
                        "Enter API Key and API Secret from http://www.dailymotion.com/profile/developer"
                                + " into API_KEY and API_SECRET in " + OAuth2ClientCredentials.class);
                System.exit(1);
            }
        }
    }

