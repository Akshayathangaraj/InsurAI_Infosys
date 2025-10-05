import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import Navbar from "../../components/Navbar";
import axios from "axios";
import styles from "./Dashboard.module.css";
import { authHeader } from "../../utils/authHeader";

const API_BASE = "http://localhost:8080/api";

const AdminDashboard = () => {
  const [claims, setClaims] = useState([]);
  const [agents, setAgents] = useState([]);
  const [policiesCount, setPoliciesCount] = useState(0);
  const [settleModal, setSettleModal] = useState({ show: false, claimId: null });
  const [settleAmount, setSettleAmount] = useState("");
  const [settleNotes, setSettleNotes] = useState("");

  const navigate = useNavigate();

  // Fetch all claims
  const fetchClaims = async () => {
    try {
      const res = await axios.get(`${API_BASE}/claims`, { headers: authHeader() });
      setClaims(res.data);
    } catch (err) {
      console.error("Error fetching claims:", err);
    }
  };

  // Fetch all agents (users with role AGENT)
  const fetchAgents = async () => {
    try {
      const res = await axios.get(`${API_BASE}/users/role/AGENT`, { headers: authHeader() });
      setAgents(res.data);
    } catch (err) {
      console.error("Error fetching agents:", err);
    }
  };

  // Fetch policies count
  const fetchPoliciesCount = async () => {
    try {
      const res = await axios.get(`${API_BASE}/policies`, { headers: authHeader() });
      setPoliciesCount(res.data.length);
    } catch (err) {
      console.error("Error fetching policies:", err);
    }
  };

  useEffect(() => {
    fetchClaims();
    fetchAgents();
    fetchPoliciesCount();
  }, []);

  // Update claim status
  const handleStatusChange = async (claimId, status) => {
    try {
      await axios.put(`${API_BASE}/claims/${claimId}/status?status=${status}`, {}, { headers: authHeader() });
      fetchClaims();
    } catch (err) {
      console.error("Error updating claim status:", err);
    }
  };

  // Assign agent to a claim
  const handleAssignAgent = async (claimId, agentId) => {
    if (!agentId) return;
    try {
      await axios.put(`${API_BASE}/claims/${claimId}/assign-agent/${agentId}`, {}, { headers: authHeader() });
      fetchClaims();
    } catch (err) {
      console.error("Error assigning agent:", err);
    }
  };

  // Open settle modal
  const openSettleModal = (claimId) => {
    setSettleModal({ show: true, claimId });
    setSettleAmount("");
    setSettleNotes("");
  };

  // Settle claim
  const handleSettleClaim = async () => {
    try {
      await axios.put(
        `${API_BASE}/claims/${settleModal.claimId}/settle`,
        {
          settlementAmount: parseFloat(settleAmount),
          resolutionNotes: settleNotes,
          processedById: localStorage.getItem("userId"),
        },
        { headers: authHeader() }
      );
      setSettleModal({ show: false, claimId: null });
      fetchClaims();
    } catch (err) {
      console.error("Error settling claim:", err);
    }
  };

  return (
    <div>
      <Navbar />
      <div className={styles.dashboardContainer}>
        <h1>Admin Dashboard</h1>

        {/* Policies Card */}
        <div className={styles.cardsContainer}>
          <div className={styles.card}>
            <h2>Total Policies</h2>
            <p>{policiesCount}</p>
            <button className={styles.btn} onClick={() => navigate("/admin/policies")}>
              Manage Policies
            </button>
          </div>
        </div>

        {/* Claims Table */}
        <h2>Claims Management</h2>
        <table className={styles.table}>
          <thead>
            <tr>
              <th>ID</th>
              <th>Employee</th>
              <th>Policy</th>
              <th>Description</th>
              <th>Status</th>
              <th>Assigned Agent</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {claims.length === 0 ? (
              <tr>
                <td colSpan="7" style={{ textAlign: "center" }}>
                  No claims available
                </td>
              </tr>
            ) : (
              claims.map((c) => (
                <tr key={c.id}>
                  <td>{c.id}</td>
                  <td>{c.employee?.user?.username || c.employeeName || "N/A"}</td>
                  <td>{c.policy?.policyName || c.policyName || `Policy #${c.policyId}`}</td>
                  <td>{c.description}</td>
                  <td>{c.status}</td>
                  <td>
                    {c.assignedAgent ? (
                      c.assignedAgent.username
                    ) : agents.length > 0 ? (
                      <select defaultValue="" onChange={(e) => handleAssignAgent(c.id, e.target.value)}>
                        <option value="" disabled>
                          Assign Agent
                        </option>
                        {agents.map((a) => (
                          <option key={a.id} value={a.id}>
                            {a.username || a.fullName || `Agent #${a.id}`}
                          </option>
                        ))}
                      </select>
                    ) : (
                      "No agents available"
                    )}
                  </td>
                  <td>
                    {c.status === "PENDING" && (
                      <>
                        <button className={styles.btn} onClick={() => handleStatusChange(c.id, "APPROVED")}>
                          Approve
                        </button>
                        <button className={styles.btnDanger} onClick={() => handleStatusChange(c.id, "REJECTED")}>
                          Reject
                        </button>
                      </>
                    )}
                    {c.status === "APPROVED" && (
                      <button className={styles.btn} onClick={() => openSettleModal(c.id)}>
                        Settle
                      </button>
                    )}
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>

        {/* Settle Claim Modal */}
        {settleModal.show && (
          <div className={styles.modal}>
            <h3>Settle Claim #{settleModal.claimId}</h3>
            <input
              type="number"
              placeholder="Settlement Amount"
              value={settleAmount}
              onChange={(e) => setSettleAmount(e.target.value)}
            />
            <textarea
              placeholder="Resolution Notes"
              value={settleNotes}
              onChange={(e) => setSettleNotes(e.target.value)}
            />
            <div className={styles.modalButtons}>
              <button className={styles.btn} onClick={handleSettleClaim}>
                Settle
              </button>
              <button className={styles.btnDanger} onClick={() => setSettleModal({ show: false, claimId: null })}>
                Cancel
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default AdminDashboard;
