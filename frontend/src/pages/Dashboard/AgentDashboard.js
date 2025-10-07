import React, { useEffect, useState, useCallback } from "react";
import axios from "axios";
import styles from "./Dashboard.module.css";
import { FaFileSignature, FaCalendarAlt, FaClock } from "react-icons/fa";
import { authHeader } from "../../utils/authHeader";
import AgentFreeTime from "./AgentFreeTime";

const API_BASE = "http://localhost:8080/api";

const AgentDashboard = () => {
  const [assignedClaims, setAssignedClaims] = useState([]);
  const [appointments, setAppointments] = useState([]);
  const [progressNote, setProgressNote] = useState("");
  const [noteModal, setNoteModal] = useState({ show: false, claimId: null });
  const [loading, setLoading] = useState(true);
  const [activePage, setActivePage] = useState("home");

  const userId = Number(localStorage.getItem("userId"));

  // Fetch assigned claims
  const fetchAssignedClaims = useCallback(async () => {
    if (!userId) return;
    try {
      const res = await axios.get(`${API_BASE}/claims/agent/${userId}`, {
        headers: authHeader(),
      });
      setAssignedClaims(res.data);
    } catch (err) {
      console.error("Error fetching claims:", err);
    } finally {
      setLoading(false);
    }
  }, [userId]);

  // Fetch appointments
  const fetchAppointments = useCallback(async () => {
    try {
      const res = await axios.get(`${API_BASE}/appointments/agent/${userId}`, {
        headers: authHeader(),
      });
      setAppointments(res.data);
    } catch (err) {
      console.error("Error fetching appointments:", err);
    }
  }, [userId]);

  useEffect(() => {
    fetchAssignedClaims();
    fetchAppointments();
  }, [fetchAssignedClaims, fetchAppointments]);

  // Add progress note
  const openNoteModal = (claimId) => {
    setNoteModal({ show: true, claimId });
    setProgressNote("");
  };

  const handleAddNote = async () => {
    if (!progressNote.trim()) return alert("Note cannot be empty");
    try {
      await axios.post(
        `${API_BASE}/claim-notes/add`,
        { claimId: noteModal.claimId, agentId: userId, note: progressNote },
        { headers: authHeader() }
      );
      setNoteModal({ show: false, claimId: null });
      fetchAssignedClaims();
    } catch (err) {
      console.error("Error adding progress note:", err);
    }
  };

  const handleStatusChange = async (appointmentId, status) => {
    try {
      await axios.put(
        `${API_BASE}/appointments/${appointmentId}/status?status=${status}`,
        {},
        { headers: authHeader() }
      );
      fetchAppointments();
    } catch (err) {
      console.error("Failed to update appointment status:", err);
    }
  };

  const getDocLink = (relativePath) => {
    const filename = relativePath.split("/").pop();
    return `${API_BASE}/claims/view-document/${filename}`;
  };

  const handleCardClick = (page) => setActivePage(page);

  return (
    <div>
      <div className={styles.dashboardWrapper}>
        {/* Home Page */}
        {activePage === "home" && (
          <div className={styles.dashboardContainer}>
            <h1 className={styles.dashboardTitle}>Welcome, Agent</h1>
            <p className={styles.subtitle}>
              Review assigned claims, appointments, and manage your schedule.
            </p>
            <div className={styles.cardsContainer}>
              {/* Assigned Claims Card */}
              <div
                className={`${styles.card} ${styles.cardGreen}`}
                onClick={() => handleCardClick("assigned-claims")}
              >
                <div className={styles.iconWrapper}>
                  <FaFileSignature className={styles.icon} />
                </div>
                <h2>Assigned Claims</h2>
                <p className={styles.count}>{assignedClaims.length}</p>
                <button className={styles.btn}>View Assigned Claims</button>
              </div>

              {/* Appointments Card */}
              <div
                className={`${styles.card} ${styles.cardPurple}`}
                onClick={() => handleCardClick("appointments")}
              >
                <div className={styles.iconWrapper}>
                  <FaCalendarAlt className={styles.icon} />
                </div>
                <h2>Appointments</h2>
                <p className={styles.count}>{appointments.length}</p>
                <button className={styles.btn}>View Appointments</button>
              </div>

              {/* Free Time Card */}
              <div
                className={`${styles.card} ${styles.cardBlue}`}
                onClick={() => handleCardClick("freetime")}
              >
                <div className={styles.iconWrapper}>
                  <FaClock className={styles.icon} />
                </div>
                <h2>Free Time</h2>
                <p className={styles.count}>Set Schedule</p>
                <button className={styles.btn}>Manage</button>
              </div>
            </div>
          </div>
        )}

        {/* Assigned Claims Page */}
        {activePage === "assigned-claims" && (
          <div className={styles.dashboardContainer}>
            <h1>Assigned Claims</h1>
            {loading ? (
              <p>Loading assigned claims...</p>
            ) : (
              <table className={styles.table}>
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Employee</th>
                    <th>Policy</th>
                    <th>Description</th>
                    <th>Status</th>
                    <th>Document</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {assignedClaims.length === 0 ? (
                    <tr>
                      <td colSpan="7" style={{ textAlign: "center" }}>
                        No assigned claims
                      </td>
                    </tr>
                  ) : (
                    assignedClaims.map((c) => (
                      <React.Fragment key={c.id}>
                        <tr>
                          <td>{c.id}</td>
                          <td>{c.employeeName || "N/A"}</td>
                          <td>{c.policyName || `Policy #${c.policyId}`}</td>
                          <td>{c.description}</td>
                          <td>{c.status}</td>
                          <td>
                            {c.documentPaths?.map((path, idx) => (
                              <a
                                key={idx}
                                href={getDocLink(path)}
                                target="_blank"
                                rel="noreferrer"
                                style={{ marginRight: 8 }}
                              >
                                View {idx + 1}
                              </a>
                            )) || "N/A"}
                          </td>
                          <td>
                            <button onClick={() => openNoteModal(c.id)}>Add Note</button>
                          </td>
                        </tr>
                        <tr>
                          <td colSpan="7">
                            <strong>Progress Notes:</strong>
                            <ul>
                              {c.notes?.length ? (
                                c.notes.map((n) => (
                                  <li key={n.id}>
                                    {n.note}{" "}
                                    <em>({new Date(n.createdAt).toLocaleString()})</em>
                                  </li>
                                ))
                              ) : (
                                <li>No notes yet</li>
                              )}
                            </ul>
                          </td>
                        </tr>
                      </React.Fragment>
                    ))
                  )}
                </tbody>
              </table>
            )}
            {noteModal.show && (
              <div className={styles.modal}>
                <h3>Add Progress Note for Claim #{noteModal.claimId}</h3>
                <textarea
                  placeholder="Enter note"
                  value={progressNote}
                  onChange={(e) => setProgressNote(e.target.value)}
                />
                <div style={{ marginTop: 10 }}>
                  <button onClick={handleAddNote}>Submit Note</button>
                  <button
                    style={{ marginLeft: 10 }}
                    onClick={() => setNoteModal({ show: false, claimId: null })}
                  >
                    Cancel
                  </button>
                </div>
              </div>
            )}
            <button
              className={styles.btnSecondary}
              style={{ marginTop: 32 }}
              onClick={() => setActivePage("home")}
            >
              ← Back
            </button>
          </div>
        )}

        {/* Appointments Page */}
        {activePage === "appointments" && (
          <div className={styles.dashboardContainer}>
            <h1>My Appointments</h1>
            {appointments.length === 0 ? (
              <p>No appointments yet</p>
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
                      <td>{a.employeeName}</td>
                      <td>{new Date(a.startTime).toLocaleString()}</td>
                      <td>{new Date(a.endTime).toLocaleString()}</td>
                      {/* Status with MISSED in red */}
                      <td style={{ color: a.status === "MISSED" ? "red" : "inherit" }}>
                        {a.status}
                      </td>
                      <td>{a.notes || "-"}</td>
                      <td>
                        {a.status === "SCHEDULED" && (
                          <>
                            <button onClick={() => handleStatusChange(a.id, "COMPLETED")}>
                              Complete
                            </button>
                            <button onClick={() => handleStatusChange(a.id, "CANCELLED")}>
                              Cancel
                            </button>
                          </>
                        )}
                        {a.status === "MISSED" && <span>Missed</span>}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
            <button
              className={styles.btnSecondary}
              style={{ marginTop: 32 }}
              onClick={() => setActivePage("home")}
            >
              ← Back
            </button>
          </div>
        )}

        {/* Free Time Page */}
        {activePage === "freetime" && (
          <div className={styles.dashboardContainer}>
            <h1>My Weekly Free Time</h1>
            <AgentFreeTime />
            <button
              className={styles.btnSecondary}
              style={{ marginTop: 32 }}
              onClick={() => setActivePage("home")}
            >
              ← Back
            </button>
          </div>
        )}
      </div>
    </div>
  );
};

export default AgentDashboard;
