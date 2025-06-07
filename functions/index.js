const functions = require("firebase-functions");
const admin = require("firebase-admin");
const nodemailer = require("nodemailer");
admin.initializeApp();

// Ganti dengan email & app password Gmail kamu
const gmailEmail = "jointheteamup@gmail.com";
const gmailAppPassword = "mmkk cwul xrgs kqcp";
const transporter = nodemailer.createTransport({
  service: "gmail",
  auth: {
    user: gmailEmail,
    pass: gmailAppPassword,
  },
});

// Fungsi kirim email verifikasi kustom menggunakan Callable Function daripada Auth Trigger
exports.sendVerificationEmail = functions.https.onCall(
  async (data, context) => {
    // Pastikan pengguna sudah login
    if (!context.auth) {
      throw new functions.https.HttpsError(
        "unauthenticated",
        "User must be logged in"
      );
    }

    const user = context.auth;
    const email = user.email;

    try {
      const link = await admin.auth().generateEmailVerificationLink(email);
      const mailOptions = {
        from: `TeamUp App <${gmailEmail}>`,
        to: email,
        subject: "Verifikasi Akun TeamUp Kamu",
        html: `
        <h2>Halo, ${user.displayName || "Pengguna Baru"}!</h2>
        <p>Terima kasih sudah mendaftar di <strong>TeamUp</strong>.</p>
        <p>Klik tombol di bawah untuk verifikasi akun kamu:</p>
        <a href="${link}" style="padding:10px 20px; background-color:#4CAF50; color:white; text-decoration:none;">Verifikasi Email</a>
        <p>Atau salin link ini ke browser kamu:</p>
        <p>${link}</p>
        <br>
        <p>Salam hangat,</p>
        <p>Team TeamUp ❤️</p>
      `,
      };

      await transporter.sendMail(mailOptions);
      console.log("Email verifikasi dikirim ke:", email);
      return { success: true };
    } catch (error) {
      console.error("Gagal mengirim email verifikasi:", error);
      throw new functions.https.HttpsError(
        "internal",
        "Failed to send verification email"
      );
    }
  }
);
