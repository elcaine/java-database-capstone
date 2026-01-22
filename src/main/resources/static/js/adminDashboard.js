/*
  adminDashboard.js
  Handles admin dashboard functionality for managing doctors.
*/

import { getDoctors, filterDoctors, saveDoctor } from "./doctorServices.js";
import { createDoctorCard } from "./doctorCard.js";
import { openModal, closeModal } from "./util.js";

document.addEventListener("DOMContentLoaded", () => {
  loadDoctorCards();

  const searchBar = document.getElementById("searchBar");
  const timeFilter = document.getElementById("timeFilter");
  const specialtyFilter = document.getElementById("specialtyFilter");

  if (searchBar) {
    searchBar.addEventListener("input", filterDoctorsOnChange);
  }
  if (timeFilter) {
    timeFilter.addEventListener("change", filterDoctorsOnChange);
  }
  if (specialtyFilter) {
    specialtyFilter.addEventListener("change", filterDoctorsOnChange);
  }
});

/**
 * Load and render all doctors
 */
async function loadDoctorCards() {
  try {
    const doctors = await getDoctors();
    renderDoctorCards(doctors);
  } catch (err) {
    console.error("Error loading doctors:", err);
  }
}

/**
 * Filter doctors based on input values
 */
async function filterDoctorsOnChange() {
  const name =
    document.getElementById("searchBar")?.value?.trim() || "null";
  const time =
    document.getElementById("timeFilter")?.value || "null";
  const specialty =
    document.getElementById("specialtyFilter")?.value || "null";

  try {
    const result = await filterDoctors(name, time, specialty);
    const doctors = result.doctors || [];

    if (doctors.length === 0) {
      document.getElementById("content").innerHTML =
        "<p>No doctors found with the given filters.</p>";
    } else {
      renderDoctorCards(doctors);
    }
  } catch (err) {
    alert("Error filtering doctors.");
  }
}

/**
 * Render doctor cards into the content area
 */
function renderDoctorCards(doctors) {
  const content = document.getElementById("content");
  content.innerHTML = "";

  doctors.forEach((doctor) => {
    const card = createDoctorCard(doctor);
    content.appendChild(card);
  });
}

/**
 * Add a new doctor (called from modal form)
 */
window.adminAddDoctor = async function () {
  const name = document.getElementById("doctorName")?.value;
  const email = document.getElementById("doctorEmail")?.value;
  const phone = document.getElementById("doctorPhone")?.value;
  const password = document.getElementById("doctorPassword")?.value;
  const specialty = document.getElementById("doctorSpecialty")?.value;
  const times = document
    .getElementById("doctorTimes")
    ?.value.split(",");

  const token = localStorage.getItem("token");
  if (!token) {
    alert("Admin session expired. Please log in again.");
    return;
  }

  const doctor = {
    name,
    email,
    phone,
    password,
    specialty,
    availableTimes: times,
  };

  try {
    const result = await saveDoctor(doctor, token);
    if (result.success) {
      alert("Doctor added successfully.");
      closeModal();
      loadDoctorCards();
    } else {
      alert(result.message || "Failed to add doctor.");
    }
  } catch (err) {
    alert("Error adding doctor.");
  }
};