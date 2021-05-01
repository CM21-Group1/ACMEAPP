package org.feup.cm.acmeapp;

public class Constants {
    public static final int KEY_SIZE = 2048;
    public static final String ANDROID_KEYSTORE = "AndroidKeyStore";
    public static final String KEY_ALGO = "RSA";
    public static final String SIGN_ALGO = "SHA256withRSA";
    public static String keyname = "myIdKey";
    public static final String PREFS_NAME = "preferences";
    public static final String PREF_UNAME = "Username";
    public static final String PREF_PASSWORD = "Password";
    public static final String PREF_USERID ="User ID";
    public static final String PREF_PUBLICKEYSP ="PublicKey Supermarket";
    public static final String baseUrl = "https://acmeapi-cm.herokuapp.com";
    public static final String vouchersUrl = "/sp/vouchers/";
    public static final String registerUrl = "/auth/register";
    public static final String loginUrl = "/auth/login";
    public static final String purchaseUrl = "/sp/purchase/";
    public static final String ACTION_SCAN = "com.google.zxing.client.android.SCAN";
    public static final int IMAGE_SIZE=900;
    public static final String ISO_SET = "ISO-8859-1";
    public static final String ENC_ALGO = "RSA/NONE/PKCS1Padding";
}
