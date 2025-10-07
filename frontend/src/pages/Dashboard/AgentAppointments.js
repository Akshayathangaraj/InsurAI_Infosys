import React, { useState, useEffect } from "react";
import axios from "axios";
import { authHeader } from "../../utils/authHeader";
import styles from "./Dashboard.module.css";

const API_BASE = "http://localhost:8080/api";

const AgentAppointments = () => {
  const [appointments, setAppointments] = useState([]);
  const [loading, setLoading] = useState(true);

  const userId = Number(localStorage.getItem("userId"));

  const fetchAppointments = async () => {
    try {
      const res = await axios.get(`${API_BASE}/appointments/agent/${userId}`, {
        headers: authHeader(),
      });
      setAppointments(res.data);
    } catch (err) {
      console.error("Error fetching appointments:", err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchAppointments();
  }, []);

  const handleStatusChange = async (appointmentId, status) => {
    try {
      await axios.put(`${API_BASE}/appointments/${appointmentId}/status?status=${status}`, {}, {
        headers: authHeader(),
      });
      fetchAppointments();
    } catch (err) {
      console.error("Failed to update status:", err);
    }
  };

  if (loading) return <p>Loading appointments...</p>;

  return (
    <div>
      <h2>My Appointments</h2>
      {appointments.length === 0 ? (
        <p>No appointments scheduled</p>
      ) : (
        <table className={styles.table}>
          <thead>
            <tr>
              <th>ID</th>
              <th>Employee</th>
              <th>Start</th>
              <th>End</th>
              <th>Status</th>
              <th>Notes</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {appointments.map((a) => (
              <tr key={a.id}>
                <td>{a.id}</td>
                {/* Use employeeName from backend or fallback to "Unknown" */}
                <td>{a.employeeName || "Unknown"}</td>
                <td>{new Date(a.startTime).toLocaleString()}</td>
                <td>{new Date(a.endTime).toLocaleString()}</td>
                <td>{a.status}</td>
                <td>{a.notes || "-"}</td>
                <td>
                  {a.status === "SCHEDULED" && (
                    <>
                      <button onClick={() => handleStatusChange(a.id, "COMPLETED")}>Complete</button>
                      <button onClick={() => handleStatusChange(a.id, "CANCELLED")}>Cancel</button>
                    </>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
};

export default AgentAppointments;
