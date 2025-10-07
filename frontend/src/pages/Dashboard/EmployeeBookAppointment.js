import React, { useState, useEffect } from "react";
import axios from "axios";
import { authHeader } from "../../utils/authHeader";
import styles from "./Dashboard.module.css";

const API_BASE = "http://localhost:8080/api";

const EmployeeBookAppointment = () => {
  const [claims, setClaims] = useState([]);
  const [selectedAgentSlots, setSelectedAgentSlots] = useState([]);
  const [selectedAgent, setSelectedAgent] = useState(null);
  const [selectedSlotIndex, setSelectedSlotIndex] = useState(null);
  const [notes, setNotes] = useState("");
  const [loading, setLoading] = useState(true);

  const storedEmployeeId = localStorage.getItem("employeeId");
  const employeeId = Number(storedEmployeeId);
  const username = localStorage.getItem("username"); // Get employee name

  useEffect(() => {
    if (!employeeId) {
      alert("Employee ID not found. Please login again.");
      return;
    }

    const loadClaims = async () => {
      try {
        const res = await axios.get(`${API_BASE}/claims/employee/${employeeId}`, {
          headers: authHeader(),
        });
        setClaims(res.data || []);
      } catch (err) {
        console.error("Failed to load claims:", err);
        alert("Unable to load your claims. Please try again.");
      } finally {
        setLoading(false);
      }
    };
    loadClaims();
  }, [employeeId]);

  const openAgentSlots = async (agentId) => {
    setSelectedAgent(agentId);
    setSelectedAgentSlots([]);
    setSelectedSlotIndex(null);
    try {
      const res = await axios.get(`${API_BASE}/appointments/agent/${agentId}/slots`, {
        headers: authHeader(),
      });
      setSelectedAgentSlots(res.data || []);
      if ((res.data || []).length === 0) {
        alert("No available slots for this agent in the upcoming days.");
      }
    } catch (err) {
      console.error("Failed to fetch agent slots:", err);
      alert("Unable to fetch slots. Try again later.");
    }
  };

  const handleBook = async () => {
    if (selectedSlotIndex === null) return alert("Select a slot first");
    const slot = selectedAgentSlots[selectedSlotIndex];
    if (!slot) return alert("Selected slot not found");

    try {
      // Send employee name along with appointment
      await axios.post(`${API_BASE}/appointments/schedule`, {
        employeeId,
        employeeName: username,
        agentId: selectedAgent,
        startTime: slot.startTime,
        endTime: slot.endTime,
        notes,
      }, { headers: authHeader() });

      alert("Appointment booked successfully!");
      setNotes("");
      setSelectedAgentSlots([]);
      setSelectedAgent(null);

      // Refresh claims
      const res = await axios.get(`${API_BASE}/claims/employee/${employeeId}`, { headers: authHeader() });
      setClaims(res.data || []);
    } catch (err) {
      console.error("Booking failed:", err);
      const message = err.response?.data || err.message || "Failed to book appointment";
      alert(`Booking failed: ${message}`);
    }
  };

  if (loading) return <div className={styles.dashboardContainer}><p>Loading...</p></div>;

  return (
    <div className={styles.dashboardContainer}>
      <h2>Book Appointment with Your Agent</h2>

      <div className={styles.card}>
        <h3>Your Claims</h3>
        {claims.length === 0 ? (
          <p>You have no claims yet.</p>
        ) : (
          <table className={styles.table}>
            <thead>
              <tr>
                <th>Claim ID</th>
                <th>Description</th>
                <th>Status</th>
                <th>Assigned Agent</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              {claims.map((c) => (
                <tr key={c.id}>
                  <td>{c.id}</td>
                  <td>{c.description}</td>
                  <td>{c.status}</td>
                  <td>{c.assignedAgentName || "Unassigned"}</td>
                  <td>
                    {c.assignedAgentId ? (
                      <button onClick={() => openAgentSlots(c.assignedAgentId)}>
                        Book Appointment
                      </button>
                    ) : (
                      <span style={{ color: "#777" }}>No agent assigned</span>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {selectedAgent && (
        <div className={styles.card} style={{ marginTop: 20 }}>
          <h3>Available Slots (next days)</h3>
          {selectedAgentSlots.length === 0 ? (
            <p>No available slots found for this agent.</p>
          ) : (
            <>
              <div style={{ marginBottom: 12 }}>
                <select
                  value={selectedSlotIndex ?? ""}
                  onChange={(e) => setSelectedSlotIndex(e.target.value === "" ? null : Number(e.target.value))}
                  style={{ padding: "8px", width: "100%", maxWidth: 420 }}
                >
                  <option value="">Choose a slot</option>
                  {selectedAgentSlots.map((s, idx) => (
                    <option key={idx} value={idx}>
                      {new Date(s.startTime).toLocaleString()} — {new Date(s.endTime).toLocaleString()}
                    </option>
                  ))}
                </select>
              </div>

              <textarea
                placeholder="Notes (optional)"
                value={notes}
                onChange={(e) => setNotes(e.target.value)}
                rows={3}
                style={{ width: "100%", maxWidth: 600, padding: 8 }}
              />

              <div style={{ marginTop: 12 }}>
                <button onClick={handleBook} style={{ marginRight: 8 }}>
                  Confirm Booking
                </button>
                <button onClick={() => { setSelectedAgent(null); setSelectedAgentSlots([]); }}>
                  Cancel
                </button>
              </div>
            </>
          )}
        </div>
      )}
    </div>
  );
};

export default EmployeeBookAppointment;
