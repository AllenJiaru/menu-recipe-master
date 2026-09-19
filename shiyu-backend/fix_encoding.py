import mysql.connector

conn = mysql.connector.connect(
    host='localhost',
    port=3307,
    user='root',
    password='root',
    database='shiyu_db',
    charset='utf8mb4'
)
cursor = conn.cursor()

# Delete corrupted entries (id 1-10) and their duplicates
corrupted_ids = list(range(1, 11))
cursor.execute("DELETE FROM recipe_category WHERE id IN (%s)" % ','.join(['%s'] * len(corrupted_ids)), corrupted_ids)

# Verify remaining entries
cursor.execute("SELECT id, name, icon, sort_order, status FROM recipe_category WHERE deleted = 0 ORDER BY sort_order")
print("Remaining categories:")
for row in cursor.fetchall():
    print(f"  {row[0]} - {row[1]} - {row[2]} - order:{row[3]} - status:{row[4]}")

conn.commit()
cursor.close()
conn.close()
