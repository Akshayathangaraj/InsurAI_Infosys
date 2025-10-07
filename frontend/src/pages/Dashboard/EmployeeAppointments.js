// src/pages/EmployeeAppointments/EmployeeAppointments.js
import React, { useEffect, useState } from "react";
import axios from "axios";
import { authHeader } from "../../utils/authHeader";
import styles from "../Dashboard/Dashboard.module.css";

const API_BASE = "http://localhost:8080/api";

const EmployeeAppointments = () => {
  const [appointments, setAppointments] = useState([]);
  const [loading, setLoading] = useState(true);
  const employeeId = localStorage.getItem("employeeId");

  useEffect(() => {
    const fetchAppointments = async () => {
      try {
        const res = await axios.get(`${API_BASE}/appointments/employee/${employeeId}`, {
          headers: authHeader(),
        });
        setAppointments(res.data || []);
      } catch (err) {
        console.error(err);
        alert("Failed to fetch appointments.");
      } finally {
        setLoading(false);
      }
    };
    fetchAppointments();
  }, [employeeId]);

  if (loading) return <div className={styles.dashboardContainer}><p>Loading...</p></div>;

  return (
    <div className={styles.dashboardWrapper}>
      <div className={styles.dashboardContainer}>
        <h2>My Appointments</h2>
        {appointments.length === 0 ? (
          <p>No appointments yet.</p>
        ) : (
          <table className={styles.table}>
            <thead>
              <tr>
                <th>ID</th>
                <th>Start Time</th>
                <th>End Time</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              {appointments.map((a) => (
                <tr key={a.id}>
                  <td>{a.id}</td>
                  <td>{new Date(a.startTime).toLocaleString()}</td>
                  <td>{new Date(a.endTime).toLocaleString()}</td>
                  <td>{a.status}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
};

export default EmployeeAppointments;
