from typing import Any, Text, Dict, List
from rasa_sdk import Action, Tracker
from rasa_sdk.executor import CollectingDispatcher
import pymysql

# Action to fetch email specifically from users table
class ActionFetchEmail(Action):

    def name(self) -> Text:
        return "action_fetch_email"

    def run(self,
            dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        user_id = tracker.get_slot("identifier")

        if not user_id:
            dispatcher.utter_message(text="Please specify a user ID to fetch the email.")
            return []

        try:
            connection = pymysql.connect(
                host="localhost",
                user="root",
                password="Akshaya@2005",
                database="insurai_db"
            )
            cursor = connection.cursor()
            query = "SELECT email FROM users WHERE id = %s"
            cursor.execute(query, (user_id,))
            result = cursor.fetchone()

            if result:
                dispatcher.utter_message(text=f"Email of user {user_id}: {result[0]}")
            else:
                dispatcher.utter_message(text=f"User with ID {user_id} not found.")

        except Exception as e:
            dispatcher.utter_message(text=f"⚠️ Error fetching email: {str(e)}")

        finally:
            if connection:
                cursor.close()
                connection.close()

        return []

# Action to fetch any column from any table dynamically
class ActionFetchTableData(Action):

    def name(self) -> Text:
        return "action_fetch_table_data"

    # Mapping of columns to their respective tables
    column_table_map = {
        # users
        "email": "users",
        "username": "users",
        "role": "users",
        "profile_photo_path": "users",
        "password": "users",
        "id": "users",
        # employees
        "full_name": "employees",
        "department": "employees",
        "designation": "employees",
        "user_id": "employees",
        "id": "employees",
        # policies
        "policy_name": "policies",
        "description": "policies",
        "premium": "policies",
        "claim_limit": "policies",
        "coverage_amount": "policies",
        "creation_date": "policies",
        "effective_date": "policies",
        "expiry_date": "policies",
        "installment_type": "policies",
        "notes": "policies",
        "policy_code": "policies",
        "policy_type": "policies",
        "renewal_notice_days": "policies",
        "risk_level": "policies",
        "status": "policies",
        "terms_and_conditions": "policies",
        # claims
        "amount": "claims",
        "claim_date": "claims",
        "description": "claims",
        "status": "claims",
        "document_path": "claims",
        "decision_date": "claims",
        "resolution_notes": "claims",
        "settlement_amount": "claims",
        "assigned_agent_id": "claims",
        "processed_by_id": "claims",
        "agent_notes": "claims",
        "agent_suggestion": "claims",
        "employee_id": "claims",
        "policy_id": "claims",
        "id": "claims",
        # appointments
        "appointment_time": "appointments",
        "start_time": "appointments",
        "end_time": "appointments",
        "status": "appointments",
        "agent_id": "appointments",
        "employee_id": "appointments",
        "policy_id": "appointments",
        "notes": "appointments",
        "id": "appointments",
        # agent_availability
        "start_time": "agent_availability",
        "end_time": "agent_availability",
        "is_booked": "agent_availability",
        "agent_id": "agent_availability",
        "day_of_week": "agent_availability",
        "is_off": "agent_availability",
        "id": "agent_availability",
        # Add remaining columns similarly...
    }

    def run(self,
            dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        column = tracker.get_slot("column")
        identifier = tracker.get_slot("identifier")

        if not column or not identifier:
            dispatcher.utter_message(text="Please specify which data you want and the ID.")
            return []

        table = self.column_table_map.get(column.lower())
        if not table:
            dispatcher.utter_message(text=f"Unknown column '{column}'. I can't find the corresponding table.")
            return []

        try:
            connection = pymysql.connect(
                host="localhost",
                user="root",
                password="Akshaya@2005",
                database="insurai_db"
            )
            cursor = connection.cursor()
            query = f"SELECT `{column}` FROM `{table}` WHERE id = %s"
            cursor.execute(query, (identifier,))
            result = cursor.fetchone()

            if result:
                dispatcher.utter_message(text=f"{column} of {table} {identifier}: {result[0]}")
            else:
                dispatcher.utter_message(text=f"No record found in table '{table}' with ID {identifier}.")

        except Exception as e:
            dispatcher.utter_message(text=f"⚠️ Error fetching data: {str(e)}")

        finally:
            if connection:
                cursor.close()
                connection.close()

        return []
