/*
  doctorServices.js
  Handles API interactions related to doctor management.
*/

import { BASE_API_URL } from "./config.js";

const DOCTOR_API = `${BASE_API_URL}/doctor`;

/**
 * Fetch all doctors
 */
export async function getDoctors() {
  try {
    const response = await fetch(DOCTOR_API);
    const data = await response.json();
    return data.doctors || [];
  } catch (error) {
    console.error("Error fetching doctors:", error);
    return [];
  }
}

/**
 * Delete a doctor by ID (admin only)
 */
export async function deleteDoctor(doctorId, token) {
  try {
    const response = await fetch(`${DOCTOR_API}/delete/${doctorId}/${token}`, {
      method: "DELETE",
    });
    const data = await response.json();
    return {
      success: true,
      message: data.message,
    };
  } catch (error) {
    console.error("Error deleting doctor:", error);
    return {
      success: false,
      message: "Failed to delete doctor.",
    };
  }
}

/**
 * Save (create) a new doctor
 */
export async function saveDoctor(doctor, token) {
  try {
    const response = await fetch(`${DOCTOR_API}/save/${token}`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(doctor),
    });

    const data = await response.json();
    return {
      success: response.ok,
      message: data.message,
    };
  } catch (error) {
    console.error("Error saving doctor:", error);
    return {
      success: false,
      message: "Failed to save doctor.",
    };
  }
}

/**
 * Filter doctors by name, time, and specialty
 */
export async function filterDoctors(name, time, specialty) {
  try {
    const response = await fetch(
      `${DOCTOR_API}/filter/${name}/${time}/${specialty}`
    );

    if (!response.ok) {
      console.error("Failed to filter doctors");
      return { doctors: [] };
    }

    const data = await response.json();
    return data;
  } catch (error) {
    alert("Error filtering doctors.");
    return { doctors: [] };
  }
}