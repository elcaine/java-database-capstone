/*
  doctorCard.js
  Renders a doctor card with role-specific actions.
*/

import { openBookingOverlay } from "./loggedPatient.js";
import { deleteDoctor } from "./doctorServices.js";
import { fetchPatientDetails } from "./patientServices.js";

export function createDoctorCard(doctor) {
  const card = document.createElement("div");
  card.classList.add("doctor-card");

  const role = localStorage.getItem("userRole");

  // Doctor info container
  const info = document.createElement("div");
  info.classList.add("doctor-info");

  const name = document.createElement("h3");
  name.textContent = doctor.name;

  const specialty = document.createElement("p");
  specialty.textContent = `Specialization: ${doctor.specialization}`;

  const email = document.createElement("p");
  email.textContent = `Email: ${doctor.email}`;

  const times = document.createElement("p");
  times.textContent = `Available Times: ${doctor.availableTimes?.join(", ") || "N/A"}`;

  info.appendChild(name);
  info.appendChild(specialty);
  info.appendChild(email);
  info.appendChild(times);

  // Actions container
  const actions = document.createElement("div");
  actions.classList.add("doctor-actions");

  // === ADMIN ROLE ACTIONS ===
  if (role === "admin") {
    const deleteBtn = document.createElement("button");
    deleteBtn.classList.add("adminBtn");
    deleteBtn.textContent = "Delete";

    deleteBtn.addEventListener("click", async () => {
      const token = localStorage.getItem("token");
      if (!token) {
        alert("Admin session expired. Please log in again.");
        return;
      }

      try {
        await deleteDoctor(doctor.id, token);
        alert("Doctor deleted successfully.");
        card.remove();
      } catch (err) {
        alert("Failed to delete doctor.");
      }
    });

    actions.appendChild(deleteBtn);
  }

  // === PATIENT (NOT LOGGED-IN) ROLE ACTIONS ===
  if (role === "patient") {
    const bookBtn = document.createElement("button");
    bookBtn.textContent = "Book Now";

    bookBtn.addEventListener("click", () => {
      alert("Please log in to book an appointment.");
    });

    actions.appendChild(bookBtn);
  }

  // === LOGGED-IN PATIENT ROLE ACTIONS ===
  if (role === "loggedPatient") {
    const bookBtn = document.createElement("button");
    bookBtn.textContent = "Book Now";

    bookBtn.addEventListener("click", async () => {
      const token = localStorage.getItem("token");
      if (!token) {
        alert("Session expired. Please log in again.");
        return;
      }

      try {
        const patient = await fetchPatientDetails(token);
        openBookingOverlay(doctor, patient);
      } catch (err) {
        alert("Unable to start booking.");
      }
    });

    actions.appendChild(bookBtn);
  }

  card.appendChild(info);
  card.appendChild(actions);

  return card;
}