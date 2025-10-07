import React, { useEffect, useState, useCallback } from "react";
import axios from "axios";
import { authHeader } from "../../utils/authHeader";
import styles from "./Dashboard.module.css";

const API_BASE = "http://localhost:8080/api";

const EmployeeClaims = () => {
  const [claims, setClaims] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [newClaim, setNewClaim] = useState({ description: "", amount: "", policyId: "" });
  const [files, setFiles] = useState([]);
  const [policies, setPolicies] = useState([]);
  const [uploadingClaimId, setUploadingClaimId] = useState(null);
  const [additionalFiles, setAdditionalFiles] = useState({}); // { claimId: File[] }

  const employeeId = localStorage.getItem("employeeId");
  const userId = localStorage.getItem("userId");

  const fetchClaims = useCallback(async () => {
    if (!employeeId) {
      setError("Employee ID missing. Please login again.");
      setLoading(false);
      return;
    }
    try {
      const res = await axios.get(`${API_BASE}/claims/employee/${employeeId}`, {
        headers: authHeader(),
      });
      setClaims(res.data);
    } catch (err) {
      console.error(err);
      setError("Failed to load claims.");
    } finally {
      setLoading(false);
    }
  }, [employeeId]);

  const fetchPolicies = useCallback(async () => {
    try {
      const res = await axios.get(`${API_BASE}/policies`, { headers: authHeader() });
      setPolicies(res.data);
    } catch (err) {
      console.error(err);
    }
  }, []);

  useEffect(() => {
    fetchPolicies();
    fetchClaims();
  }, [fetchPolicies, fetchClaims]);

  const handleChange = (e) => setNewClaim({ ...newClaim, [e.target.name]: e.target.value });
  const handleFileChange = (e) => setFiles(Array.from(e.target.files));
  const handleAdditionalFilesChange = (e, claimId) => {
    setAdditionalFiles({ ...additionalFiles, [claimId]: Array.from(e.target.files) });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!employeeId) return alert("Employee ID missing. Please login again.");

    try {
      const formData = new FormData();
      formData.append("description", newClaim.description);
      formData.append("amount", newClaim.amount);
      formData.append("policyId", newClaim.policyId);
      formData.append("employeeId", employeeId);
      files.forEach((file) => formData.append("documents", file));

      await axios.post(`${API_BASE}/claims/submit`, formData, {
        headers: { ...authHeader(), "Content-Type": "multipart/form-data" },
      });

      alert("✅ Claim submitted successfully!");
      setNewClaim({ description: "", amount: "", policyId: "" });
      setFiles([]);
      fetchClaims();
    } catch (err) {
      console.error(err);
      alert("❌ Error submitting claim.");
    }
  };

  const handleAddFiles = async (claimId, e) => {
    e.preventDefault();
    if (!additionalFiles[claimId]?.length) return;
    if (!userId || userId === "null") return alert("User ID missing. Please login again.");

    try {
      const formData = new FormData();
      additionalFiles[claimId].forEach((file) => formData.append("documents", file));
      formData.append("userId", userId);

      await axios.post(`${API_BASE}/claims/${claimId}/add-files`, formData, {
        headers: { ...authHeader(), "Content-Type": "multipart/form-data" },
      });

      alert("✅ Files added successfully!");
      setAdditionalFiles({ ...additionalFiles, [claimId]: [] });
      setUploadingClaimId(null);
      fetchClaims();
    } catch (err) {
      console.error(err);
      alert("❌ Error adding files.");
    }
  };

  return (
    <div className={styles.dashboardContainer}>
      <h1 className={styles.pageTitle}>📑 My Claims</h1>

      {/* Claim submission form */}
      <form onSubmit={handleSubmit} className={styles.cardForm}>
        <h2 className={styles.formTitle}>Submit a New Claim</h2>
        <input
          type="text"
          name="description"
          placeholder="Claim Description"
          value={newClaim.description}
          onChange={handleChange}
          required
        />
        <input
          type="number"
          name="amount"
          placeholder="Claim Amount"
          value={newClaim.amount}
          onChange={handleChange}
          required
        />
        <select name="policyId" value={newClaim.policyId} onChange={handleChange} required>
          <option value="">Select Policy</option>
          {policies.map((p) => (
            <option key={p.id} value={p.id}>{p.policyName}</option>
          ))}
        </select>
        <input type="file" multiple onChange={handleFileChange} />
        {files.length > 0 && (
          <div className={styles.selectedFiles}>
            {files.map((f, idx) => <span key={idx} className={styles.fileBadge}>{f.name}</span>)}
          </div>
        )}
        <button type="submit" className={styles.btnPrimary}>Submit Claim</button>
      </form>

      {/* Claims display */}
      {loading ? (
        <p className={styles.loading}>⏳ Loading claims...</p>
      ) : error ? (
        <p className={styles.error}>{error}</p>
      ) : claims.length === 0 ? (
        <p className={styles.emptyState}>No claims submitted yet.</p>
      ) : (
        <div className={styles.claimsGrid}>
          {claims.map((claim) => (
            <div key={claim.id} className={styles.claimCard}>
              <div className={styles.claimHeader}>
                <h3>Claim #{claim.id}</h3>
                <span className={`${styles.status} ${styles[claim.status.toLowerCase()]}`}>{claim.status}</span>
              </div>

              <div className={styles.claimBody}>
                <p><b>Description:</b> {claim.description}</p>
                <p><b>Amount:</b> ${claim.amount}</p>
                <p><b>Policy:</b> {claim.policyName || "N/A"}</p>
                <p><b>Submitted On:</b> {new Date(claim.claimDate).toLocaleString()}</p>
                <p><b>Assigned Agent:</b> {claim.assignedAgentName || "Not assigned"}</p>
              </div>

              {/* Progress Notes & Add Files Section */}
              <div className={styles.notesSection}>
                <h4>📌 Progress Notes</h4>
                {claim.notes?.length ? (
                  <ul className={styles.notesList}>
                    {claim.notes.map((note) => (
                      <li key={note.id} className={styles.noteItem}>
                        <div className={styles.noteHeader}>
                          <b>{note.agentName || "System"}</b>
                          <span className={styles.noteDate}>{new Date(note.createdAt).toLocaleString()}</span>
                        </div>
                        <p>{note.note}</p>
                      </li>
                    ))}
                  </ul>
                ) : <p className={styles.noNotes}>No updates yet</p>}

                {claim.status !== "SETTLED" && (
                  <div className={styles.addFilesSection}>
                    {uploadingClaimId === claim.id ? (
                      <form onSubmit={(e) => handleAddFiles(claim.id, e)}>
                        <input type="file" multiple onChange={(e) => handleAdditionalFilesChange(e, claim.id)} />
                        {additionalFiles[claim.id]?.length > 0 && (
                          <div className={styles.selectedFiles}>
                            {additionalFiles[claim.id].map((f, idx) => <span key={idx} className={styles.fileBadge}>{f.name}</span>)}
                          </div>
                        )}
                        <button type="submit" className={styles.btnSecondary}>Upload Files</button>
                        <button type="button" className={styles.btnSecondary} onClick={() => setUploadingClaimId(null)}>Cancel</button>
                      </form>
                    ) : (
                      <button className={styles.btnSecondary} onClick={() => setUploadingClaimId(claim.id)}>Add Files</button>
                    )}
                  </div>
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default EmployeeClaims;
