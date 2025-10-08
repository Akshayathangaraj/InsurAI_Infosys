import React, { useEffect, useState, useCallback } from "react";
import axios from "axios";
import styles from "./Dashboard.module.css";
import { authHeader } from "../../utils/authHeader";
import { useNavigate } from "react-router-dom";
import { FaShieldAlt, FaFileSignature, FaCalendarAlt } from "react-icons/fa";
import Chatbot from "../../components/Chatbot"; // import Chatbot component

const UserDashboard = () => {
  const navigate = useNavigate();
  const [policies, setPolicies] = useState([]);
  const [claims, setClaims] = useState([]);
  const [appointments, setAppointments] = useState([]);
  const [loading, setLoading] = useState(true);

  const API_BASE = "http://localhost:8080/api";
  const username = localStorage.getItem("username");
  const storedEmployeeId = localStorage.getItem("employeeId");

  const fetchData = useCallback(async () => {
    try {
      let employeeId = storedEmployeeId;
      if (!employeeId) {
        const empRes = await axios.get(`${API_BASE}/employees/by-username/${username}`, {
          headers: authHeader(),
        });
        employeeId = empRes.data.id;
        localStorage.setItem("employeeId", employeeId);
      }

      const [policyRes, claimRes, appRes] = await Promise.all([
        axios.get(`${API_BASE}/policies/employee/${employeeId}`, { headers: authHeader() }),
        axios.get(`${API_BASE}/claims/employee/${employeeId}`, { headers: authHeader() }),
        axios.get(`${API_BASE}/appointments/employee/${employeeId}`, { headers: authHeader() }),
      ]);

      setPolicies(policyRes.data || []);
      setClaims(claimRes.data || []);
      setAppointments(appRes.data || []);
    } catch (err) {
      console.error("Error fetching dashboard data:", err);
      alert("Failed to load dashboard data. Please try again.");
    } finally {
      setLoading(false);
    }
  }, [storedEmployeeId, username]);

  useEffect(() => {
    fetchData();
  }, [fetchData]);

  if (loading) {
    return (
      <div className={styles.dashboardWrapper}>
        <div className={styles.dashboardContainer}>
          <h2 className={styles.subtitle}>Loading your dashboard...</h2>
        </div>
      </div>
    );
  }

  return (
    <div className={styles.dashboardWrapper}>
      <div className={styles.dashboardContainer}>
        <h1 className={styles.dashboardTitle}>Welcome to Your Dashboard</h1>
        <p className={styles.subtitle}>Manage your policies, track claims, and check your appointments with ease.</p>

        <div className={styles.cardsContainer}>
          <div className={`${styles.card} ${styles.cardBlue}`}>
            <div className={styles.iconWrapper}><FaShieldAlt className={styles.icon} /></div>
            <h2>My Policies</h2>
            <p className={styles.count}>{policies.length}</p>
            <button className={styles.btn} onClick={() => navigate("/employee-policies")}>View Policies</button>
          </div>

          <div className={`${styles.card} ${styles.cardGreen}`}>
            <div className={styles.iconWrapper}><FaFileSignature className={styles.icon} /></div>
            <h2>My Claims</h2>
            <p className={styles.count}>{claims.length}</p>
            <button className={styles.btn} onClick={() => navigate("/employee-claims")}>View Claims</button>
          </div>

          <div className={`${styles.card} ${styles.cardPurple}`}>
            <div className={styles.iconWrapper}><FaCalendarAlt className={styles.icon} /></div>
            <h2>My Appointments</h2>
            <p className={styles.count}>{appointments.length}</p>
            <div className={styles.btnGroup}>
              <button className={styles.btn} onClick={() => navigate("/employee-appointments")}>View Appointments</button>
              <button className={`${styles.btn} ${styles.btnSecondary}`} onClick={() => navigate("/book-appointment")}>Book Appointment</button>
            </div>
          </div>
        </div>
      </div>

      {/* Chatbot Floating Component */}
      <Chatbot />
    </div>
  );
};

export default UserDashboard;
