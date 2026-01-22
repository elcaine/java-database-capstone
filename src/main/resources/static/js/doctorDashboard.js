/*
  doctorDashboard.js
  Handles loading and filtering patient appointments for doctors.
*/

import { getAllAppointments } from "./appointmentServices.js";
import { createPatientRow } from "./patientRow.js";

const tableBody = document.getElementById("patientTableBody");
const searchBar = document.getElementById("searchBar");
const todayButton = document.getElementById("todayButton");
const datePicker = document.getElementById("datePicker");

let selectedDate = new Date().toISOString().split("T")[0];
let patientName = "null";
const token = localStorage.getItem("token");

// Search by patient name
if (searchBar) {
  searchBar.addEventListener("input", () => {
    const value = searchBar.value.trim();
    patientName = value !== "" ? value : "null";
    loadAppointments();
  });
}

// Show today's appointments
if (todayButton) {
  todayButton.addEventListener("click", () => {
    selectedDate = new Date().toISOString().split("T")[0];
    if (datePicker) {
      datePicker.value = selectedDate;
    }
    loadAppointments();
  });
}

// Change appointment date
if (datePicker) {
  datePicker.addEventListener("change", () => {
    selectedDate = datePicker.value;
    loadAppointments();
  });
}

// Fetch and render appointments
async function loadAppointments() {
  tableBody.innerHTML = "";

  try {
    const appointments = await getAllAppointments(
      selectedDate,
      patientName,
      token
    );

    if (!appointments || appointments.length === 0) {
      tableBody.innerHTML = `
        <tr>
          <td colspan="5" class="noPatientRecord">
            No Appointments found for today.
          </td>
        </tr>
      `;
      return;
    }

    appointments.forEach((appt) => {
      const patient = {
        id: appt.patientId,
        name: appt.patientName,
        phone: appt.phone,
        email: appt.email,
        prescription: appt.prescription,
      };

      const row = createPatientRow(patient);
      tableBody.appendChild(row);
    });
  } catch (error) {
    tableBody.innerHTML = `
      <tr>
        <td colspan="5" class="noPatientRecord">
          Error loading appointments. Try again later.
        </td>
      </tr>
    `;
  }
}

// Initialize on page load
document.addEventListener("DOMContentLoaded", () => {
  renderContent();
  if (datePicker) {
    datePicker.value = selectedDate;
  }
  loadAppointments();
});