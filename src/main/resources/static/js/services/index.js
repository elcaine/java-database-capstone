/*
  index.js
  Handles role selection and login logic for the landing page.
*/

import { openModal } from "./util.js";
import { BASE_API_URL } from "./config.js";

const ADMIN_API = `${BASE_API_URL}/admin/login`;
const DOCTOR_API = `${BASE_API_URL}/doctor/login`;

window.onload = () => {
  const adminLoginBtn = document.getElementById("adminLogin");
  const doctorLoginBtn = document.getElementById("doctorLogin");

  if (adminLoginBtn) {
    adminLoginBtn.addEventListener("click", () => openModal("adminLogin"));
  }

  if (doctorLoginBtn) {
    doctorLoginBtn.addEventListener("click", () => openModal("doctorLogin"));
  }
};

window.adminLoginHandler = async function () {
  const username = document.getElementById("adminUsername")?.value;
  const password = document.getElementById("adminPassword")?.value;

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
    selectRole("admin");
  } catch (err) {
    alert("Unable to log in. Please try again later.");
  }
};

window.doctorLoginHandler = async function () {
  const email = document.getElementById("doctorEmail")?.value;
  const password = document.getElementById("doctorPassword")?.value;

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
    selectRole("doctor");
  } catch (err) {
    console.error(err);
    alert("Unable to log in. Please try again later.");
  }
};