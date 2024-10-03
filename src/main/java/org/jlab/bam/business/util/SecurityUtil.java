package org.jlab.bam.business.util;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.*;

public class SecurityUtil {

  private static final SSLSocketFactory defaultFactory =
      (SSLSocketFactory) SSLSocketFactory.getDefault();
  private static final HostnameVerifier defaultVerifier =
      HttpsURLConnection.getDefaultHostnameVerifier();

  private SecurityUtil() {
    // Can't instantiate publicly
  }

  /**
   * Disables the server certificate check performed when using the default SSLSocketFactory.
   *
   * @throws NoSuchAlgorithmException If unable to disable
   * @throws KeyManagementException If unable to disable
   */
  public static void disableServerCertificateCheck()
      throws NoSuchAlgorithmException, KeyManagementException {
    SSLSocketFactory factory = getTrustySocketFactory();
    HttpsURLConnection.setDefaultSSLSocketFactory(factory);

    HttpsURLConnection.setDefaultHostnameVerifier(
        new HostnameVerifier() {
          @Override
          public boolean verify(String s, SSLSession sslSession) {
            return true;
          }
        });
  }

  /**
   * Re-enables the server certificate check performed when using the default SSLSocketFactory, if
   * it was previously disabled.
   */
  public static void enableServerCertificateCheck() {
    HttpsURLConnection.setDefaultSSLSocketFactory(defaultFactory);

    HttpsURLConnection.setDefaultHostnameVerifier(defaultVerifier);
  }

  /** A X509TrustManager which trusts every certificate regardless of attributes. */
  public static class TrustyTrustManager implements X509TrustManager {

    @Override
    public void checkClientTrusted(X509Certificate[] xcs, String string)
        throws CertificateException {}

    @Override
    public void checkServerTrusted(X509Certificate[] xcs, String string)
        throws CertificateException {}

    @Override
    public X509Certificate[] getAcceptedIssuers() {
      return null;
    }
  }

  /**
   * Obtains a custom SSLSocketFactory which trusts every certificate regardless of attributes.
   *
   * @return A trusty SSLSocketFactory
   * @throws NoSuchAlgorithmException If unable to obtain an SSLContext
   * @throws KeyManagementException If Unable to initialize the SSLContext
   */
  public static SSLSocketFactory getTrustySocketFactory()
      throws NoSuchAlgorithmException, KeyManagementException {
    SSLContext context = SSLContext.getInstance("TLS");

    context.init(null, new TrustManager[] {new TrustyTrustManager()}, null);

    return context.getSocketFactory();
  }
}
