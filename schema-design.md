# MySQL Database Design

### Table: appointments
- id: INT, Primary Key, Auto Increment
- doctor_id: INT, Foreign Key → doctors(id)
- patient_id: INT, Foreign Key → patients(id)
- location_id: INT, Foreign Key → clinic_locations(id)
- appointment_time: DATETIME, Not Null
- status: INT (0 = Scheduled, 1 = Completed, 2 = Cancelled)

### Table: patients
- id: INT, Primary Key, Auto Increment

### Table: doctors
- id: INT, Primary Key, Auto Increment

### Table: admin
- id: INT, Primary Key, Auto Increment

### Table: clinic_locations
- id: INT, Primary Key, Auto Increment
- address: VARCHAR(255), Not Null
















# MongoDB Collection Design

### Collection: prescriptions
```json
{
  "_id": "ObjectId('64abc123456')",
  "patientName": "John Smith",
  "appointmentId": 51,
  "medication": "Paracetamol",
  "dosage": "500mg",
  "doctorNotes": "Take 1 tablet every 6 hours.",
  "refillCount": 2,
  "pharmacy": {
    "name": "Walgreens SF",
    "location": "Market Street"
  }
}