import React, { useEffect, useState } from "react";
import Navbar from "../../components/Navbar";
import axios from "axios";
import styles from "./Dashboard.module.css";
import { authHeader } from "../../utils/authHeader";

const PolicyManagement = () => {
  const [policies, setPolicies] = useState([]);
  const [employees, setEmployees] = useState([]);
  const [newPolicy, setNewPolicy] = useState({
    policyName: "",
    policyCode: "",
    policyType: "HEALTH",
    status: "ACTIVE",
    description: "",
    premium: "",
    coverageAmount: "",
    installmentType: "MONTHLY",
    termsAndConditions: "",
    riskLevel: "LOW",
    claimLimit: "",
    renewalNoticeDays: 30,
    notes: "",
    effectiveDate: "",
    expiryDate: "",
  });
  const [editingPolicyId, setEditingPolicyId] = useState(null);
  const API_BASE = "http://localhost:8080/api";

  const fetchPolicies = async () => {
    const res = await axios.get(`${API_BASE}/policies`, { headers: authHeader() });
    setPolicies(res.data);
  };

  const fetchEmployees = async () => {
    const res = await axios.get(`${API_BASE}/employees`, { headers: authHeader() });
    setEmployees(res.data);
  };

  useEffect(() => {
    fetchPolicies();
    fetchEmployees();
  }, []);

  const handleCreateOrUpdatePolicy = async () => {
    try {
      if (editingPolicyId) {
        await axios.put(`${API_BASE}/policies/${editingPolicyId}`, newPolicy, { headers: authHeader() });
        setEditingPolicyId(null);
      } else {
        await axios.post(`${API_BASE}/policies/create`, newPolicy, { headers: authHeader() });
      }
      setNewPolicy({
        policyName: "",
        policyCode: "",
        policyType: "HEALTH",
        status: "ACTIVE",
        description: "",
        premium: "",
        coverageAmount: "",
        installmentType: "MONTHLY",
        termsAndConditions: "",
        riskLevel: "LOW",
        claimLimit: "",
        renewalNoticeDays: 30,
        notes: "",
        effectiveDate: "",
        expiryDate: "",
      });
      fetchPolicies();
    } catch (err) {
      console.error("Error saving policy:", err);
    }
  };

  const handleEditPolicy = (policy) => {
    setEditingPolicyId(policy.id);
    setNewPolicy(policy);
  };

  const handleDeletePolicy = async (policyId) => {
    await axios.delete(`${API_BASE}/policies/${policyId}`, { headers: authHeader() });
    fetchPolicies();
  };

  const handleAssignPolicy = async (policyId, employeeId) => {
    if (!employeeId) return;
    await axios.put(`${API_BASE}/policies/${policyId}/assign/${employeeId}`, {}, { headers: authHeader() });
    fetchPolicies();
  };

  return (
    <div>
      <Navbar />
      <div className={styles.dashboardContainer}>
        <h1>Policy Management</h1>

        {/* Create/Edit Policy Form */}
        <div className={styles.formGroup}>
          <input placeholder="Policy Name" value={newPolicy.policyName}
            onChange={(e) => setNewPolicy({ ...newPolicy, policyName: e.target.value })} />
          <input placeholder="Policy Code" value={newPolicy.policyCode}
            onChange={(e) => setNewPolicy({ ...newPolicy, policyCode: e.target.value })} />
          <select value={newPolicy.policyType}
            onChange={(e) => setNewPolicy({ ...newPolicy, policyType: e.target.value })}>
            <option value="HEALTH">Health</option>
            <option value="LIFE">Life</option>
            <option value="VEHICLE">Vehicle</option>
            <option value="PROPERTY">Property</option>
          </select>
          <select value={newPolicy.status}
            onChange={(e) => setNewPolicy({ ...newPolicy, status: e.target.value })}>
            <option value="ACTIVE">Active</option>
            <option value="EXPIRED">Expired</option>
            <option value="PENDING">Pending</option>
            <option value="CANCELLED">Cancelled</option>
          </select>
          <input type="date" value={newPolicy.effectiveDate}
            onChange={(e) => setNewPolicy({ ...newPolicy, effectiveDate: e.target.value })} />
          <input type="date" value={newPolicy.expiryDate}
            onChange={(e) => setNewPolicy({ ...newPolicy, expiryDate: e.target.value })} />
          <input type="number" placeholder="Premium" value={newPolicy.premium}
            onChange={(e) => setNewPolicy({ ...newPolicy, premium: e.target.value })} />
          <input type="number" placeholder="Coverage Amount" value={newPolicy.coverageAmount}
            onChange={(e) => setNewPolicy({ ...newPolicy, coverageAmount: e.target.value })} />
          <select value={newPolicy.installmentType}
            onChange={(e) => setNewPolicy({ ...newPolicy, installmentType: e.target.value })}>
            <option value="MONTHLY">Monthly</option>
            <option value="QUARTERLY">Quarterly</option>
            <option value="YEARLY">Yearly</option>
          </select>
          <textarea placeholder="Terms and Conditions"
            value={newPolicy.termsAndConditions}
            onChange={(e) => setNewPolicy({ ...newPolicy, termsAndConditions: e.target.value })} />
          <select value={newPolicy.riskLevel}
            onChange={(e) => setNewPolicy({ ...newPolicy, riskLevel: e.target.value })}>
            <option value="LOW">Low</option>
            <option value="MEDIUM">Medium</option>
            <option value="HIGH">High</option>
          </select>
          <input type="number" placeholder="Claim Limit" value={newPolicy.claimLimit}
            onChange={(e) => setNewPolicy({ ...newPolicy, claimLimit: e.target.value })} />
          <input type="number" placeholder="Renewal Notice Days" value={newPolicy.renewalNoticeDays}
            onChange={(e) => setNewPolicy({ ...newPolicy, renewalNoticeDays: e.target.value })} />
          <input type="text" placeholder="Notes" value={newPolicy.notes}
            onChange={(e) => setNewPolicy({ ...newPolicy, notes: e.target.value })} />

          <button onClick={handleCreateOrUpdatePolicy}>
            {editingPolicyId ? "Update Policy" : "Add Policy"}
          </button>
        </div>

        {/* Policies Table */}
        <table className={styles.table}>
          <thead>
            <tr>
              <th>Code</th>
              <th>Name</th>
              <th>Type</th>
              <th>Status</th>
              <th>Premium</th>
              <th>Coverage</th>
              <th>Effective</th>
              <th>Expiry</th>
              <th>Employees</th>
              <th>Assign</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {policies.map((p) => (
              <tr key={p.id}>
                <td>{p.policyCode}</td>
                <td>{p.policyName}</td>
                <td>{p.policyType}</td>
                <td>{p.status}</td>
                <td>{p.premium}</td>
                <td>{p.coverageAmount}</td>
                <td>{p.effectiveDate}</td>
                <td>{p.expiryDate}</td>
                <td>{p.assignedEmployees?.map(emp => emp.user?.username).join(", ") || "Unassigned"}</td>
                <td>
                  <select onChange={(e) => handleAssignPolicy(p.id, e.target.value)} defaultValue="">
                    <option value="">Select Employee</option>
                    {employees.map(emp => (
                      <option key={emp.id} value={emp.id}>{emp.user?.username}</option>
                    ))}
                  </select>
                </td>
                <td>
                  <button onClick={() => handleEditPolicy(p)}>Edit</button>
                  <button onClick={() => handleDeletePolicy(p.id)}>Delete</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default PolicyManagement;
