import urllib.request
import urllib.parse
import json
import time
import sys

BASE_URL = "http://localhost:8081/api"

def make_request(method, url, headers=None, data=None):
    if headers is None:
        headers = {}
    if data is not None:
        data = json.dumps(data).encode('utf-8')
        headers['Content-Type'] = 'application/json'
    
    req = urllib.request.Request(url, data=data, headers=headers, method=method)
    try:
        with urllib.request.urlopen(req, timeout=10) as response:
            status = response.getcode()
            body = response.read().decode('utf-8')
            try:
                json_body = json.loads(body)
            except json.JSONDecodeError:
                json_body = body
            return status, json_body
    except urllib.error.HTTPError as e:
        body = e.read().decode('utf-8')
        try:
            json_body = json.loads(body)
        except json.JSONDecodeError:
            json_body = body
        return e.code, json_body
    except Exception as e:
        return 500, str(e)

def wait_for_server():
    print("Waiting for server to start...")
    sys.stdout.flush()
    for _ in range(30):
        try:
            make_request("GET", "http://localhost:8081/api-docs")
            print("Server is up!")
            sys.stdout.flush()
            return True
        except Exception:
            time.sleep(2)
    print("Server failed to start.")
    sys.stdout.flush()
    return False

def log_response(file, endpoint, method, status, response):
    file.write(f"Endpoint: {method} {endpoint}\n")
    file.write(f"Status: {status}\n")
    file.write(f"Response: {json.dumps(response, indent=2)}\n")
    file.write("-" * 50 + "\n")
    print(f"Tested {method} {endpoint}: {status}")
    sys.stdout.flush()

def login(username, password):
    url = f"{BASE_URL}/auth/login"
    payload = {"username": username, "password": password}
    status, response = make_request("POST", url, data=payload)
    if status == 200:
        return response.get("token")
    else:
        print(f"Login failed for {username}. Status: {status}")
        sys.stdout.flush()
        return None

def main():
    if not wait_for_server():
        return

    # Try different users
    admin_token = login("admin", "Admin@123")
    waiter_token = login("waiter_rahul", "Waiter@123")
    
    # Try to register a new user and use their token
    tester_token = None
    new_user_name = f"tester_{int(time.time())}"
    reg_payload = {
        "username": new_user_name,
        "password": "Password@123",
        "email": f"tester_{int(time.time())}@example.com",
        "phoneNumber": "1234567890",
        "fullName": "Tester User"
    }
    status, resp = make_request("POST", f"{BASE_URL}/auth/register", data=reg_payload)
    print(f"Registration status: {status}")
    sys.stdout.flush()
    
    if status == 201:
        tester_token = resp.get("token")
        print("Obtained token from registration.")
    
    # Determine which tokens to use
    if not admin_token and tester_token:
        print("Using tester token as fallback for admin (will fail on restricted endpoints)")
    
    token_to_use = admin_token if admin_token else (tester_token if tester_token else waiter_token)
    headers = {"Authorization": f"Bearer {token_to_use}"} if token_to_use else {}

    print(f"Starting tests with token: {token_to_use[:10] if token_to_use else 'None'}...")
    sys.stdout.flush()

    with open("endpoint_responses.txt", "w", encoding='utf-8') as f:
        # 1. Auth Controller - Register (already done, logging result)
        log_response(f, "/auth/register", "POST", status, resp)

        # 2. User Management
        status, resp = make_request("GET", f"{BASE_URL}/users", headers=headers)
        log_response(f, "/users", "GET", status, resp)
        
        # 3. Category Controller
        # Create Category
        cat_payload = {"name": f"Test Category {int(time.time())}", "description": "Test Description", "active": True}
        status, resp = make_request("POST", f"{BASE_URL}/categories", headers=headers, data=cat_payload)
        log_response(f, "/categories", "POST", status, resp)
        
        category_id = None
        if status == 201:
            category_id = resp.get("id")
            
        status, resp = make_request("GET", f"{BASE_URL}/categories", headers=headers)
        log_response(f, "/categories", "GET", status, resp)
        # Assuming we can find at least one category to use
        if not category_id and status == 200 and isinstance(resp, list) and len(resp) > 0:
            category_id = resp[0].get("id")

        # 4. Table Controller
        table_payload = {"name": f"T-{int(time.time()) % 1000}", "capacity": 4, "status": "AVAILABLE"}
        status, resp = make_request("POST", f"{BASE_URL}/tables", headers=headers, data=table_payload)
        log_response(f, "/tables", "POST", status, resp)
        
        table_id = None
        if status == 201:
            table_id = resp.get("id")
        
        status, resp = make_request("GET", f"{BASE_URL}/tables", headers=headers)
        log_response(f, "/tables", "GET", status, resp)
        if not table_id and status == 200 and isinstance(resp, list) and len(resp) > 0:
            table_id = resp[0].get("id")
            
        if table_id:
             status, resp = make_request("GET", f"{BASE_URL}/tables/{table_id}", headers=headers)
             log_response(f, f"/tables/{table_id}", "GET", status, resp)

        # 5. Menu Item Controller
        if category_id:
            menu_payload = {
                "name": f"Chicken Wings {int(time.time())}",
                "description": "Spicy wings",
                "price": 12.99,
                "imageUrl": "http://example.com/wings.jpg",
                "available": True,
                "category": {"id": category_id}
            }
            status, resp = make_request("POST", f"{BASE_URL}/menu-items", headers=headers, data=menu_payload)
            log_response(f, "/menu-items", "POST", status, resp)
            
            if status == 201:
                menu_item_id = resp.get("id")
                status, resp = make_request("GET", f"{BASE_URL}/menu-items/{menu_item_id}", headers=headers)
                log_response(f, f"/menu-items/{menu_item_id}", "GET", status, resp)

        status, resp = make_request("GET", f"{BASE_URL}/menu-items", headers=headers)
        log_response(f, "/menu-items", "GET", status, resp)

        # 6. Booking Controller
        if table_id:
            booking_payload = {
                "customerName": "John Doe",
                "customerPhone": "9876543210",
                "bookingDateTime": "2026-12-31T20:00:00",
                "partySize": 2,
                "tableId": table_id
            }
            status, resp = make_request("POST", f"{BASE_URL}/bookings", headers=headers, data=booking_payload)
            log_response(f, "/bookings", "POST", status, resp)
            
            status, resp = make_request("GET", f"{BASE_URL}/bookings", headers=headers)
            log_response(f, "/bookings", "GET", status, resp)

        # 7. Kitchen Controller
        status, resp = make_request("GET", f"{BASE_URL}/kitchen/orders/pending", headers=headers)
        log_response(f, "/kitchen/orders/pending", "GET", status, resp)

        # 8. Report Controller
        status, resp = make_request("GET", f"{BASE_URL}/reports/order-status-count", headers=headers)
        log_response(f, "/reports/order-status-count", "GET", status, resp)

if __name__ == "__main__":
    main()
