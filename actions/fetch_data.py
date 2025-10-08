from rasa_sdk import Action, Tracker
from rasa_sdk.executor import CollectingDispatcher
import pymysql

# Example action to get user email by user_id
class ActionGetUserEmail(Action):
    def name(self) -> str:
        return "action_fetch_email"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: dict):

        # Get user_id from slot
        user_id = tracker.get_slot("user_id")

        # Connect to MySQL
        connection = pymysql.connect(
            host='localhost',       # your MySQL host
            user='root',            # MySQL username
            password='Akshaya@2005',    # MySQL password
            database='insurai_db'      # your database
        )

        cursor = connection.cursor()
        cursor.execute("SELECT email FROM users WHERE id=%s", (user_id,))
        result = cursor.fetchone()
        cursor.close()
        connection.close()

        if result:
            dispatcher.utter_message(text=f"Email of user {user_id}: {result[0]}")
        else:
            dispatcher.utter_message(text="User not found!")

        return []
