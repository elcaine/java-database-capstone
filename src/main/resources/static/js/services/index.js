/*
  index.js
  Handles role selection and login logic for the landing page.
*/

import { openModal } from "../components/modals.js";
import { API_BASE_URL } from "../config/config.js";

const ADMIN_API = `${API_BASE_URL}/admin/login`;
const DOCTOR_API = `${API_BASE_URL}/doctor/login`;

window.onload = () => {
  const adminLoginBtn = document.getElementById("adminLogin");
  const doctorLoginBtn = document.getElementById("doctorLogin");
  const patientLoginBtn = document.getElementById("patientLogin");

  if (adminLoginBtn) {
    adminLoginBtn.addEventListener("click", () => openModal("adminLogin"));
  }

  if (doctorLoginBtn) {
    doctorLoginBtn.addEventListener("click", () => openModal("doctorLogin"));
  }

  // Supported by modals.js in the template; harmless if button doesn't exist
  if (patientLoginBtn) {
    patientLoginBtn.addEventListener("click", () => openModal("patientLogin"));
  }
};

window.adminLoginHandler = async function () {
  // modals.js uses id="username" and id="password" for admin login
  const username = document.getElementById("username")?.value;
  const password = document.getElementById("password")?.value;

  if (!username || !password) {
    alert("Please enter username and password.");
    return;
  }

  const admin = { username, password };

  try {
    const response = await fetch(ADMIN_API, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(admin),
    });

    if (!response.ok) {
      alert("Invalid admin credentials.");
      return;
    }

    const data = await response.json();
    localStorage.setItem("token", data.token);
    localStorage.setItem("userRole", "admin");

    // selectRole is typically defined elsewhere in the template JS
    if (typeof window.selectRole === "function") {
      window.selectRole("admin");
    }
  } catch (err) {
    console.error(err);
    alert("Unable to log in. Please try again later.");
  }
};

window.doctorLoginHandler = async function () {
  // modals.js uses id="email" and id="password" for doctor login
  const email = document.getElementById("email")?.value;
  const password = document.getElementById("password")?.value;

  if (!email || !password) {
    alert("Please enter email and password.");
    return;
  }

  const doctor = { email, password };

  try {
    const response = await fetch(DOCTOR_API, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(doctor),
    });

    if (!response.ok) {
      alert("Invalid doctor credentials.");
      return;
    }

    const data = await response.json();
    localStorage.setItem("token", data.token);
    localStorage.setItem("userRole", "doctor");

    if (typeof window.selectRole === "function") {
      window.selectRole("doctor");
    }
  } catch (err) {
    console.error(err);
    alert("Unable to log in. Please try again later.");
  }
};