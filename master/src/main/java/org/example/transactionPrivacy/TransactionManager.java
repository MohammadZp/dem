
package org.example;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;

/**
 * Responsible for securely saving and loading transaction data.
 *
 * The important security requirements are:
 *
 * 1. Transaction data must NOT be stored as plain text.
 * 2. The encryption key must NOT be hard-coded.
 * 3. The password is obtained from the TX_PASSWORD environment variable.
 * 4. The password is converted into an AES key using PBKDF2.
 * 5. A new random Salt is generated for every transaction.
 * 6. A new random IV is generated for every encryption.
 * 7. AES/GCM is used for authenticated encryption.
 * 8. Salt and IV are stored together with the encrypted data.
 *    They are not secrets and are required during decryption.
 *
 * File format:
 *
 *     [salt length]
 *     [salt]
 *     [IV length]
 *     [IV]
 *     [encrypted data + GCM authentication tag]
 */
public class TransactionManager {

    /*
     * The file where the encrypted transaction will be stored.
     *
     * The actual transaction data is never written to this file
     * as plain text.
     */
    private static final Path FILE = Path.of("transaction.dat");

    /*
     * Name of the environment variable containing the password.
     *
     * We don't store the actual password here.
     *
     * Example:
     *
     * Linux/macOS:
     *     export TX_PASSWORD=my-secret
     *
     * Windows PowerShell:
     *     $env:TX_PASSWORD="my-secret"
     */
    private static final String ENV_PASSWORD = "TX_PASSWORD";

    /*
     * Required by the challenge:
     *
     * If TX_PASSWORD is not configured, use a default password.
     *
     * NOTE:
     * This is acceptable for this exercise because the requirement
     * explicitly asks for a default value.
     *
     * In a real production system, a hard-coded default password
     * would be a security weakness. A secret manager or mandatory
     * environment variable would be preferable.
     */
    private static final String DEFAULT_PASSWORD = "default-password";

    /*
     * Salt size in bytes.
     *
     * Salt is random data used by PBKDF2 when deriving the AES key.
     *
     * Salt does NOT need to be secret, so it can safely be stored
     * in transaction.dat.
     */
    private static final int SALT_LENGTH = 16;

    /*
     * GCM normally uses a 12-byte IV.
     *
     * IV = Initialization Vector.
     *
     * The IV does NOT need to be secret, but it must be unique/random
     * for every encryption operation when using GCM.
     *
     * Therefore we generate a new IV every time saveTransaction()
     * is called.
     */
    private static final int IV_LENGTH = 12;

    /*
     * AES key size.
     *
     * PBKDF2 will derive 256 bits = 32 bytes.
     */
    private static final int KEY_LENGTH = 256;

    /*
     * Number of PBKDF2 iterations.
     *
     * PBKDF2 intentionally performs many calculations so that
     * brute-force password guessing becomes more expensive.
     *
     * Higher values generally increase security but also increase
     * CPU cost during encryption/decryption.
     */
    private static final int ITERATIONS = 100_000;

    /*
     * GCM authentication tag length.
     *
     * 128 bits = 16 bytes.
     *
     * The authentication tag allows AES/GCM to detect whether the
     * ciphertext was modified or whether the wrong key was used.
     */
    private static final int GCM_TAG_LENGTH = 128;

    /*
     * SecureRandom is used instead of java.util.Random.
     *
     * SecureRandom is designed for cryptographic random values and
     * is appropriate for generating Salt and IV.
     */
    private final SecureRandom secureRandom = new SecureRandom();


    /**
     * Encrypts the transaction data and stores it in transaction.dat.
     *
     * High-level flow:
     *
     *     Plain text
     *          |
     *          v
     *     Get password
     *          |
     *          v
     *     Generate random Salt
     *          |
     *          v
     *     PBKDF2(password + Salt)
     *          |
     *          v
     *     AES key
     *          |
     *          +---- random IV
     *          |
     *          v
     *     AES/GCM encryption
     *          |
     *          v
     *     Salt + IV + Ciphertext
     *          |
     *          v
     *     transaction.dat
     */
    public void saveTransaction(String data) {

        try {

            /*
             * Read the password from the environment.
             *
             * IMPORTANT:
             * We never store the password in transaction.dat.
             */
            String password = getPassword();


            /*
             * Generate a new random Salt.
             *
             * The Salt prevents the same password from always producing
             * the same AES key.
             *
             * Salt is NOT secret, so it will be stored in the file.
             */
            byte[] salt = new byte[SALT_LENGTH];
            secureRandom.nextBytes(salt);


            /*
             * Generate a new random IV.
             *
             * The IV is required by AES/GCM.
             *
             * A fresh IV is generated for every encryption.
             *
             * The IV does not need to be secret, so it can also be
             * stored in the encrypted file.
             */
            byte[] iv = new byte[IV_LENGTH];
            secureRandom.nextBytes(iv);


            /*
             * Derive the AES encryption key from the password.
             *
             * We do NOT use the password directly as an AES key.
             *
             * Instead:
             *
             *     password + salt
             *             |
             *             v
             *           PBKDF2
             *             |
             *             v
             *       256-bit AES key
             */
            SecretKey key = deriveKey(password, salt);


            /*
             * Create an AES cipher using GCM mode.
             *
             * AES = encryption algorithm
             * GCM = authenticated encryption mode
             *
             * "NoPadding" is standard for GCM.
             */
            Cipher cipher =
                    Cipher.getInstance("AES/GCM/NoPadding");


            /*
             * Configure GCM with:
             *
             * - authentication tag length = 128 bits
             * - the randomly generated IV
             */
            GCMParameterSpec gcmSpec =
                    new GCMParameterSpec(GCM_TAG_LENGTH, iv);


            /*
             * Initialize cipher for ENCRYPTION.
             *
             * The cipher now has:
             *
             *     AES key
             *     IV
             *     GCM configuration
             */
            cipher.init(
                    Cipher.ENCRYPT_MODE,
                    key,
                    gcmSpec
            );


            /*
             * Convert the transaction String into UTF-8 bytes.
             *
             * Encryption works on bytes, not directly on Java Strings.
             */
            byte[] plainText =
                    data.getBytes(StandardCharsets.UTF_8);


            /*
             * Encrypt the transaction.
             *
             * AES/GCM also creates an authentication tag.
             *
             * Therefore encryptedData contains:
             *
             *     ciphertext + authentication tag
             */
            byte[] encryptedData =
                    cipher.doFinal(plainText);


            /*
             * We now need to store all information required for
             * decryption.
             *
             * We store:
             *
             *     Salt
             *     IV
             *     Ciphertext
             *
             * We DO NOT store:
             *
             *     Password
             *     AES key
             *
             * File structure:
             *
             *     [salt length]
             *     [salt]
             *     [IV length]
             *     [IV]
             *     [encrypted data]
             */
            ByteBuffer buffer = ByteBuffer.allocate(
                    Integer.BYTES
                            + salt.length
                            + Integer.BYTES
                            + iv.length
                            + encryptedData.length
            );


            /*
             * Store the Salt length first.
             *
             * This tells the reader how many bytes belong to Salt.
             */
            buffer.putInt(salt.length);

            /*
             * Store Salt itself.
             */
            buffer.put(salt);


            /*
             * Store IV length.
             *
             * This tells the reader how many bytes belong to IV.
             */
            buffer.putInt(iv.length);

            /*
             * Store IV itself.
             */
            buffer.put(iv);


            /*
             * Finally store the encrypted transaction.
             */
            buffer.put(encryptedData);


            /*
             * Write the binary data to transaction.dat.
             *
             * The file now contains binary encrypted data rather than
             * the original transaction text.
             */
            Files.write(FILE, buffer.array());

        } catch (GeneralSecurityException | IOException e) {

            /*
             * Convert checked exceptions into a RuntimeException so
             * callers don't have to handle all crypto/file exceptions.
             *
             * In a larger application, a custom exception would often
             * be preferable.
             */
            throw new RuntimeException(
                    "Failed to save transaction",
                    e
            );
        }
    }


    /**
     * Loads and decrypts the transaction from transaction.dat.
     *
     * High-level flow:
     *
     *     transaction.dat
     *          |
     *          +---- Salt
     *          +---- IV
     *          +---- Ciphertext
     *                    |
     *                    v
     *               Get password
     *                    |
     *                    v
     *             PBKDF2 + Salt
     *                    |
     *                    v
     *                AES key
     *                    |
     *                    v
     *              AES/GCM decrypt
     *                    |
     *                    v
     *             Original text
     *
     * If the password is incorrect, PBKDF2 produces a different
     * AES key and GCM authentication will fail.
     */
    public String loadTransaction() {

        try {

            /*
             * Read the complete encrypted file as bytes.
             */
            byte[] fileData =
                    Files.readAllBytes(FILE);


            /*
             * ByteBuffer makes it convenient to read the data
             * in the exact order in which we stored it.
             */
            ByteBuffer buffer =
                    ByteBuffer.wrap(fileData);


            /*
             * First value in the file is the Salt length.
             */
            int saltLength =
                    buffer.getInt();


            /*
             * Read the Salt.
             *
             * Salt is not secret and is required to reproduce
             * the same AES key from the password.
             */
            byte[] salt =
                    new byte[saltLength];

            buffer.get(salt);


            /*
             * Next value is the IV length.
             */
            int ivLength =
                    buffer.getInt();


            /*
             * Read the IV.
             */
            byte[] iv =
                    new byte[ivLength];

            buffer.get(iv);


            /*
             * Everything remaining in the file is the encrypted data.
             *
             * This includes the GCM authentication tag.
             */
            byte[] encryptedData =
                    new byte[buffer.remaining()];

            buffer.get(encryptedData);


            /*
             * Obtain the password again.
             *
             * It must be the same password that was used during
             * encryption.
             */
            String password =
                    getPassword();


            /*
             * Derive the same AES key using:
             *
             *     password + stored Salt
             *
             * If the password is correct, this produces the same
             * AES key that was used during encryption.
             *
             * If the password is wrong, a different key is produced.
             */
            SecretKey key =
                    deriveKey(password, salt);


            /*
             * Create the same AES/GCM cipher used during encryption.
             */
            Cipher cipher =
                    Cipher.getInstance("AES/GCM/NoPadding");


            /*
             * Reconstruct the GCM parameters using the stored IV.
             */
            GCMParameterSpec gcmSpec =
                    new GCMParameterSpec(
                            GCM_TAG_LENGTH,
                            iv
                    );


            /*
             * Initialize cipher for DECRYPTION.
             */
            cipher.init(
                    Cipher.DECRYPT_MODE,
                    key,
                    gcmSpec
            );


            /*
             * Decrypt the ciphertext.
             *
             * GCM verifies the authentication tag here.
             *
             * If:
             *
             *     - password is wrong
             *     - ciphertext was modified
             *     - authentication tag is invalid
             *
             * doFinal() will throw a security exception.
             */
            byte[] decryptedData =
                    cipher.doFinal(encryptedData);


            /*
             * Convert decrypted UTF-8 bytes back into a String.
             */
            return new String(
                    decryptedData,
                    StandardCharsets.UTF_8
            );

        } catch (GeneralSecurityException | IOException e) {

            /*
             * A wrong password will normally end up here because
             * AES/GCM authentication fails.
             *
             * We intentionally don't return partially decrypted data.
             */
            throw new RuntimeException(
                    "Failed to decrypt transaction. "
                            + "Check the password or encrypted file.",
                    e
            );
        }
    }


    /**
     * Converts a password into a cryptographically strong AES key.
     *
     * Why do we need this method?
     *
     * A human password is not necessarily suitable as an AES key.
     *
     * Therefore we use:
     *
     *     Password
     *        +
     *      Salt
     *        |
     *        v
     *      PBKDF2
     *        |
     *        v
     *    256-bit key
     *
     * PBKDF2 intentionally performs many iterations to make
     * password guessing/brute-force attacks more expensive.
     */
    private SecretKey deriveKey(
            String password,
            byte[] salt
    ) throws GeneralSecurityException {

        /*
         * PBEKeySpec holds the information required by PBKDF2:
         *
         * - password
         * - salt
         * - iteration count
         * - desired key length
         */
        PBEKeySpec spec =
                new PBEKeySpec(
                        password.toCharArray(),
                        salt,
                        ITERATIONS,
                        KEY_LENGTH
                );


        /*
         * Create the PBKDF2 implementation provided by Java.
         *
         * PBKDF2WithHmacSHA256 means:
         *
         *     Password-Based Key Derivation Function 2
         *     using HMAC-SHA256 internally.
         */
        SecretKeyFactory factory =
                SecretKeyFactory.getInstance(
                        "PBKDF2WithHmacSHA256"
                );


        /*
         * Generate the raw key bytes.
         *
         * Because KEY_LENGTH = 256, this produces a 256-bit key.
         */
        byte[] keyBytes =
                factory
                        .generateSecret(spec)
                        .getEncoded();


        /*
         * Tell Java that these bytes should be used as an AES key.
         */
        return new SecretKeySpec(
                keyBytes,
                "AES"
        );
    }


    /**
     * Retrieves the encryption password.
     *
     * First we try to read TX_PASSWORD from the environment.
     *
     * If it doesn't exist or is blank, the default password is used.
     *
     * Importantly, this method does NOT read the password from
     * transaction.dat.
     */
    private String getPassword() {

        /*
         * Read environment variable:
         *
         *     TX_PASSWORD
         */
        String password =
                System.getenv(ENV_PASSWORD);


        /*
         * If the environment variable doesn't exist or is empty,
         * fall back to the default required by the challenge.
         */
        if (password == null || password.isBlank()) {
            return DEFAULT_PASSWORD;
        }


        /*
         * Otherwise return the configured password.
         */
        return password;
    }
}

