import React from "react";
import { Routes, Route, Navigate } from "react-router-dom";

// Pages
import Login from "./pages/Login/Login";
import Signup from "./pages/Signup/Signup";
import AdminDashboard from "./pages/Dashboard/AdminDashboard";
import UserDashboard from "./pages/Dashboard/UserDashboard";
import AgentDashboard from "./pages/Dashboard/AgentDashboard";
import EmployeePolicies from "./pages/Dashboard/EmployeePolicies";
import EmployeeClaims from "./pages/Dashboard/EmployeeClaims";
// Auth helper
import PolicyManagement from "./pages/Dashboard/PolicyManagement";

const PrivateRoute = ({ children }) => {
  const token = localStorage.getItem("token");
  return token ? children : <Navigate to="/login" />;
};

// Role-based private route
const RoleRoute = ({ children, allowedRoles }) => {
  const token = localStorage.getItem("token");
  const role = localStorage.getItem("role");
  if (!token) return <Navigate to="/login" />;
  if (allowedRoles.includes(role)) return children;
  return <Navigate to="/login" />;
};

function App() {
  return (
    <Routes>
      <Route path="/" element={<Navigate to="/login" />} />
      <Route path="/login" element={<Login />} />
      <Route path="/signup" element={<Signup />} />

      {/* Admin Dashboard */}
      <Route
        path="/admin-dashboard"
        element={
          <RoleRoute allowedRoles={["ADMIN"]}>
            <AdminDashboard />
          </RoleRoute>
        }
      />

      {/* Employee/User Dashboard */}
      <Route
        path="/user-dashboard"
        element={
          <RoleRoute allowedRoles={["EMPLOYEE"]}>
            <UserDashboard />
          </RoleRoute>
        }
      />
      <Route path="/employee-claims" element={<EmployeeClaims />} />
      <Route path="/admin/policies" element={<PolicyManagement />} />
      <Route
  path="/employee-policies"
  element={
    <PrivateRoute>
      <EmployeePolicies />
    </PrivateRoute>
  }
/>
      {/* Agent Dashboard */}
      <Route
        path="/agent-dashboard"
        element={
          <RoleRoute allowedRoles={["AGENT"]}>
            <AgentDashboard />
          </RoleRoute>
        }
      />

      {/* Fallback */}
      <Route path="*" element={<Navigate to="/login" />} />
    </Routes>
  );
}

export default App;
