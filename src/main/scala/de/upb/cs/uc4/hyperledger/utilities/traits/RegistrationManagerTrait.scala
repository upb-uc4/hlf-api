package de.upb.cs.uc4.hyperledger.utilities.traits

import java.nio.file.Path
import java.security.PrivateKey
import java.util

import de.upb.cs.uc4.hyperledger.utilities.helper.{ Logger, PublicExceptionHelper }
import org.hyperledger.fabric.gateway.{ Identities, X509Identity }
import org.hyperledger.fabric.sdk.{ Enrollment, User }
import org.hyperledger.fabric_ca.sdk.{ HFCAClient, RegistrationRequest }

trait RegistrationManagerTrait {

  /** Register a new User with the CA
    *
    * @param caURL Address to find the CA.
    * @param caCert Certificate to check the validity of the CA.
    * @param userName Name of the new user.
    * @param adminName Name of the Admin User you want to access to perform the registration.
    * @param adminWalletPath Wallet containing the admin-certificate.
    * @param maxEnrollments Number of times the user can be enrolled/re-enrolled with the username-password combination.
    * @param newUserType Permission Level of the new User. Default :: Client.
    * @throws Exception if
    *                   1. The admin user could not be retrieved from the wallet.
    *                   2. The CA Client could not be retrieved from the caURL and Certificate
    *                   3. The registration process fails. Your admin user probably has insufficient permissions.
    * @return the newly created password to use for the user when enrolling.
    */
  @throws[Exception]
  def register(
      caURL: String,
      caCert: Path,
      userName: String,
      adminName: String,
      adminWalletPath: Path,
      maxEnrollments: Integer = 1,
      newUserType: String = HFCAClient.HFCA_TYPE_CLIENT
  ): String
}
