import React, { useEffect, useState, useCallback } from "react";
import Navbar from "../../components/Navbar";
import axios from "axios";
import styles from "./Dashboard.module.css";
import { FaFileSignature, FaCalendarAlt, FaClock } from "react-icons/fa";
import { useNavigate } from "react-router-dom";
import { authHeader } from "../../utils/authHeader";

const API_BASE = "http://localhost:8080/api";

const AgentDashboard = () => {
  const [assignedClaims, setAssignedClaims] = useState([]);
  const [progressNote, setProgressNote] = useState("");
  const [noteModal, setNoteModal] = useState({ show: false, claimId: null });
  const [loading, setLoading] = useState(true);

  // Card navigation state, default to home view
  const [activePage, setActivePage] = useState("home");

  const userId = Number(localStorage.getItem("userId"));

  const fetchAssignedClaims = useCallback(async () => {
    if (!userId) return;
    try {
      const res = await axios.get(`${API_BASE}/claims/agent/${userId}`, {
        headers: authHeader(),
      });
      setAssignedClaims(res.data);
    } catch (err) {
      if (err.response?.status === 403) {
        alert("Access denied. Please login again.");
      } else {
        console.error("Error fetching assigned claims:", err);
      }
    } finally {
      setLoading(false);
    }
  }, [userId]);

  useEffect(() => {
    fetchAssignedClaims();
  }, [fetchAssignedClaims]);

  const openNoteModal = (claimId) => {
    setNoteModal({ show: true, claimId });
    setProgressNote("");
  };

  const handleAddNote = async () => {
    if (!progressNote.trim()) return alert("Note cannot be empty");

    try {
      const res = await axios.post(
        `${API_BASE}/claim-notes/add`,
        { claimId: noteModal.claimId, agentId: userId, note: progressNote },
        { headers: authHeader() }
      );
      if (res.status === 200 || res.status === 201) {
        setNoteModal({ show: false, claimId: null });
        fetchAssignedClaims();
      } else {
        alert("Failed to add note. Backend returned error.");
      }
    } catch (err) {
      console.error("Error adding progress note:", err);
      alert("Failed to add note. Try again.");
    }
  };

  // Helper for document links
  const getDocLink = (relativePath) => {
    const filename = relativePath.split("/").pop();
    return `http://localhost:8080/api/claims/view-document/${filename}`;
  };

  // Card click handlers
  const handleCardClick = (page) => setActivePage(page);

  // Modern dashboard card container
  return (
    <div>
      <Navbar />
      <div className={styles.dashboardWrapper}>
        {activePage === "home" ? (
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
                <button className={styles.btn}>
                  View Assigned Claims
                </button>
              </div>
              {/* Appointments Card (future) */}
              <div className={`${styles.card} ${styles.cardPurple}`}>
                <div className={styles.iconWrapper}>
                  <FaCalendarAlt className={styles.icon} />
                </div>
                <h2>Appointments</h2>
                <p className={styles.count}>0</p>
                <button className={styles.btn} disabled>
                  Coming Soon
                </button>
              </div>
              {/* Free Time Card (future) */}
              <div className={`${styles.card} ${styles.cardBlue}`}>
                <div className={styles.iconWrapper}>
                  <FaClock className={styles.icon} />
                </div>
                <h2>Free Time</h2>
                <p className={styles.count}>0</p>
                <button className={styles.btn} disabled>
                  Coming Soon
                </button>
              </div>
            </div>
          </div>
        ) : activePage === "assigned-claims" ? (
          <div className={styles.dashboardContainer}>
            <h1>Assigned Claims</h1>
            {loading ? (
              <p className={styles.loading}>Loading assigned claims...</p>
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
                            {c.documentPaths && c.documentPaths.length > 0
                              ? c.documentPaths.map((path, idx) => (
                                  <a
                                    key={idx}
                                    href={getDocLink(path)}
                                    target="_blank"
                                    rel="noreferrer"
                                    style={{ marginRight: "8px" }}
                                  >
                                    View {idx + 1}
                                  </a>
                                ))
                              : c.documentPath ? (
                                <a
                                  href={getDocLink(c.documentPath)}
                                  target="_blank"
                                  rel="noreferrer"
                                >
                                  View
                                </a>
                              ) : "N/A"}
                          </td>
                          <td>
                            <button onClick={() => openNoteModal(c.id)}>Add Note</button>
                          </td>
                        </tr>
                        <tr>
                          <td colSpan="7">
                            <strong>Progress Notes:</strong>
                            <ul>
                              {c.notes && c.notes.length > 0 ? (
                                c.notes.map((n) => (
                                  <li key={n.id}>
                                    {n.note} <em>({new Date(n.createdAt).toLocaleString()})</em>
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
                <div style={{ marginTop: "10px" }}>
                  <button onClick={handleAddNote}>Submit Note</button>
                  <button
                    onClick={() => setNoteModal({ show: false, claimId: null })}
                    style={{ marginLeft: "10px" }}
                  >
                    Cancel
                  </button>
                </div>
              </div>
            )}
            <button
              className={`${styles.btn} ${styles.btnSecondary}`}
              style={{ marginTop: 32 }}
              onClick={() => setActivePage("home")}
            >
              ← Back to Dashboard
            </button>
          </div>
        ) : null}
      </div>
    </div>
  );
};

export default AgentDashboard;
