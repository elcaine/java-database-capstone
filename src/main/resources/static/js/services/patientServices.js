// patientServices
import { API_BASE_URL } from "../config/config.js";
const PATIENT_API = API_BASE_URL + "/patient";

// For creating a patient in db
export async function patientSignup(data) {
  try {
    const response = await fetch(`${PATIENT_API}`, {
      method: "POST",
      headers: {
        "Content-type": "application/json",
      },
      body: JSON.stringify(data),
    });

    const result = await response.json();

    if (!response.ok) {
      throw new Error(result.message);
    }

    return { success: response.ok, message: result.message };
  } catch (error) {
    console.error("Error :: patientSignup :: ", error);
    return { success: false, message: error.message };
  }
}

// For logging in patient
export async function patientLogin(data) {
  console.log("patientLogin :: ", data);
  return await fetch(`${PATIENT_API}/login`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(data),
  });
}

// For getting patient data (name ,id , etc ). Used in booking appointments
export async function getPatientData(token) {
  try {
    const response = await fetch(`${PATIENT_API}/${token}`);
    const data = await response.json();
    if (response.ok) return data.patient;
    return null;
  } catch (error) {
    console.error("Error fetching patient details:", error);
    return null;
  }
}

// the Backend API for fetching the patient record(visible in Doctor Dashboard) and Appointments (visible in Patient Dashboard)
// are same based on user(patient/doctor).
export async function getPatientAppointments(id, token, user) {
  try {
    const response = await fetch(`${PATIENT_API}/${id}/${user}/${token}`);
    const data = await response.json();
    console.log(data.appointments);
    if (response.ok) {
      return data.appointments;
    }
    return null;
  } catch (error) {
    console.error("Error fetching patient details:", error);
    return null;
  }
}

export async function filterAppointments(condition, name, token) {
  try {
    const response = await fetch(
      `${PATIENT_API}/filter/${condition}/${name}/${token}`,
      {
        method: "GET",
        headers: {
          "Content-Type": "application/json",
        },
      }
    );

    if (response.ok) {
      const data = await response.json();
      return data;
    } else {
      console.error("Failed to fetch doctors:", response.statusText);
      return { appointments: [] };
    }
  } catch (error) {
    console.error("Error:", error);
    alert("Something went wrong!");
    return { appointments: [] };
  }
}

/**
 * ------------------------------------------------------------------
 * Template-required globals for modals.js
 * modals.js calls loginPatient() and signupPatient() directly.
 * We bridge those names to the module exports above.
 * ------------------------------------------------------------------
 */

window.signupPatient = async function signupPatient() {
  const name = document.getElementById("name")?.value?.trim();
  const email = document.getElementById("email")?.value?.trim();
  const password = document.getElementById("password")?.value;
  const phone = document.getElementById("phone")?.value?.trim();
  const address = document.getElementById("address")?.value?.trim();

  if (!name || !email || !password || !phone || !address) {
    alert("Please fill in all fields.");
    return;
  }

  const payload = { name, email, password, phone, address };
  const result = await patientSignup(payload);

  alert(result?.message || "Signup completed.");

  if (result?.success) {
    // Close modal if present (matches template element id)
    const modal = document.getElementById("modal");
    if (modal) modal.style.display = "none";
  }
};

window.loginPatient = async function loginPatient() {
  const email = document.getElementById("email")?.value?.trim();
  const password = document.getElementById("password")?.value;

  if (!email || !password) {
    alert("Please enter email and password.");
    return;
  }

  try {
    const response = await patientLogin({ email, password });

    if (!response.ok) {
      // Backend may return JSON with message; fall back to generic
      let msg = "Invalid patient credentials.";
      try {
        const body = await response.json();
        if (body?.message) msg = body.message;
      } catch (_) {}
      alert(msg);
      return;
    }

    const data = await response.json();
    if (data?.token) {
      localStorage.setItem("token", data.token);
      localStorage.setItem("userRole", "patient");
    }

    // Close modal if present
    const modal = document.getElementById("modal");
    if (modal) modal.style.display = "none";

    // If the template defines selectRole, use it (consistent with your index.js pattern)
    if (typeof window.selectRole === "function") {
      window.selectRole("patient");
    }
  } catch (err) {
    console.error(err);
    alert("Unable to log in. Please try again later.");
  }
};
