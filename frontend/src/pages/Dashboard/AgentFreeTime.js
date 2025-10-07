import React, { useEffect, useState } from "react";
import axios from "axios";
import { authHeader } from "../../utils/authHeader";
import styles from "./Dashboard.module.css";

const API_BASE = "http://localhost:8080/api";
const dayNames = ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"];
const dayMap = { "Sunday": 0, "Monday": 1, "Tuesday": 2, "Wednesday": 3, "Thursday": 4, "Friday": 5, "Saturday": 6 };

const AgentFreeTime = () => {
  const [slots, setSlots] = useState([]);
  const [newSlot, setNewSlot] = useState({ id: null, dayOfWeek: "", startTime: "", endTime: "", isOff: false });
  const [loading, setLoading] = useState(true);
  const agentId = Number(localStorage.getItem("userId"));

  const fetchSlots = async () => {
    try {
      const res = await axios.get(`${API_BASE}/agent-availability/agent/${agentId}`, { headers: authHeader() });
      setSlots(res.data);
    } catch (err) {
      console.error(err);
      alert("Failed to fetch slots");
    } finally {
      setLoading(false);
    }
  };

  const saveSlot = async () => {
    if (!newSlot.dayOfWeek) return alert("Select a day");
    if (!newSlot.isOff && (!newSlot.startTime || !newSlot.endTime)) return alert("Start and end times required");

    const payload = {
      id: newSlot.id || null,
      agentId,
      dayOfWeek: dayMap[newSlot.dayOfWeek],
      startTime: newSlot.startTime || "",
      endTime: newSlot.endTime || "",
      off: newSlot.isOff
    };

    try {
      await axios.post(`${API_BASE}/agent-availability/save`, payload, { headers: authHeader() });
      setNewSlot({ id: null, dayOfWeek: "", startTime: "", endTime: "", isOff: false });
      fetchSlots();
    } catch (err) {
      console.error(err);
      alert(err.response?.data || "Failed to save slot");
    }
  };

  const deleteSlot = async (id) => {
    if (!window.confirm("Delete this slot?")) return;
    try {
      await axios.delete(`${API_BASE}/agent-availability/${id}`, { headers: authHeader() });
      fetchSlots();
    } catch (err) {
      console.error(err);
      alert("Failed to delete slot");
    }
  };

  const toggleOff = async (slot) => {
    try {
      await axios.post(`${API_BASE}/agent-availability/toggle-off/${slot.id}`, {}, { headers: authHeader() });
      fetchSlots();
    } catch (err) {
      console.error(err);
      alert("Failed to update off status");
    }
  };

  useEffect(() => {
    fetchSlots();
  }, []);

  if (loading) return <p>Loading...</p>;

  return (
    <div className={styles.dashboardContainer}>
      <h2>Weekly Free Time Schedule</h2>

      <div className={styles.slotForm}>
        <select value={newSlot.dayOfWeek} onChange={e => setNewSlot({ ...newSlot, dayOfWeek: e.target.value })}>
          <option value="">Select Day</option>
          {dayNames.map(d => <option key={d} value={d}>{d}</option>)}
        </select>

        {!newSlot.isOff && <>
          <input type="time" value={newSlot.startTime || ""} onChange={e => setNewSlot({ ...newSlot, startTime: e.target.value })} />
          <input type="time" value={newSlot.endTime || ""} onChange={e => setNewSlot({ ...newSlot, endTime: e.target.value })} />
        </>}

        <label className={styles.offLabel}>
          <input type="checkbox" checked={newSlot.isOff} onChange={e => setNewSlot({ ...newSlot, isOff: e.target.checked })} />
          Mark as Off
        </label>

        <button className={styles.saveBtn} onClick={saveSlot}>{newSlot.id ? "Update" : "Add"}</button>
      </div>

      <table className={styles.table}>
        <thead>
          <tr>
            <th>Day</th>
            <th>Start</th>
            <th>End</th>
            <th>Status</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {slots.map(slot => (
            <tr key={slot.id} className={slot.off ? styles.offRow : ""}>
              <td>{dayNames[slot.dayOfWeek]}</td>
              <td>{slot.startTime || "-"}</td>
              <td>{slot.endTime || "-"}</td>
              <td>
                <button className={slot.off ? styles.offBtn : styles.onBtn} onClick={() => toggleOff(slot)}>
                  {slot.off ? "Off" : "On"}
                </button>
              </td>
              <td>
                <button className={styles.editBtn} onClick={() => setNewSlot({ ...slot, isOff: slot.off })}>Edit</button>
                <button className={styles.deleteBtn} onClick={() => deleteSlot(slot.id)}>Delete</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

export default AgentFreeTime;
