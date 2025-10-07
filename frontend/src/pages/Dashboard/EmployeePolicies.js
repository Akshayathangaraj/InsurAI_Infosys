import React, { useEffect, useState } from "react";
import axios from "axios";
import styles from "./Dashboard.module.css";
import { authHeader } from "../../utils/authHeader";
import { FaShieldAlt, FaMoneyBillWave, FaCalendarAlt } from "react-icons/fa";

const EmployeePolicies = () => {
  const [policies, setPolicies] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const API_BASE = "http://localhost:8080/api";
  const username = localStorage.getItem("username");

  const fetchPolicies = async () => {
    try {
      const empRes = await axios.get(`${API_BASE}/employees/by-username/${username}`, {
        headers: authHeader(),
      });

      const employeeId = empRes.data.id;
      const res = await axios.get(`${API_BASE}/policies/employee/${employeeId}`, {
        headers: authHeader(),
      });

      setPolicies(res.data);
      setLoading(false);
    } catch (err) {
      console.error(err);
      setError("Failed to load policies.");
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchPolicies();
  }, []);

  return (
    <div className={styles.dashboardWrapper}>
      <div className={styles.dashboardContainer}>
        <h1 className={styles.dashboardTitle}>My Insurance Policies</h1>
        <p className={styles.subtitle}>View details of policies assigned to you</p>

        {loading ? (
          <p className={styles.loading}>Loading policies...</p>
        ) : error ? (
          <p className={styles.error}>{error}</p>
        ) : policies.length === 0 ? (
          <p className={styles.emptyState}>No policies assigned yet.</p>
        ) : (
          <div className={styles.policyGrid}>
            {policies.map((policy) => (
              <div className={`${styles.policyCard} ${styles.fadeIn}`} key={policy.id}>
                <div className={styles.policyHeader}>
                  <div className={styles.iconWrapper}>
                    <FaShieldAlt className={styles.icon} />
                  </div>
                  <h3>{policy.policyName}</h3>
                  <span className={`${styles.statusTag} ${styles[policy.status?.toLowerCase()]}`}>
                    {policy.status}
                  </span>
                </div>

                <div className={styles.policyBody}>
                  <p><strong>Code:</strong> {policy.policyCode}</p>
                  <p><strong>Type:</strong> {policy.policyType}</p>
                  <p><strong>Coverage:</strong> ₹{policy.coverageAmount}</p>
                  <p><strong>Premium:</strong> ₹{policy.premium}</p>
                  <div className={styles.dateRow}>
                    <div className={styles.dateItem}>
                      <FaCalendarAlt className={styles.dateIcon} />
                      <span>Effective: {policy.effectiveDate}</span>
                    </div>
                    <div className={styles.dateItem}>
                      <FaCalendarAlt className={styles.dateIcon} />
                      <span>Expiry: {policy.expiryDate}</span>
                    </div>
                  </div>
                </div>

                <button className={styles.viewBtn}>
                  <FaMoneyBillWave className={styles.btnIcon} /> View Details
                </button>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default EmployeePolicies;
