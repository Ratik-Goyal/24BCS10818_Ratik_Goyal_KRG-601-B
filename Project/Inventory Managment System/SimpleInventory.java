import java.io.*;
import java.net.*;
import java.util.*;

class Item {
    int id;
    String name;
    double price;
    int quantity;
    Item left, right;

    Item(int id, String name, double price, int quantity) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }
}

class BST {
    Item root;

    void insert(int id, String name, double price, int quantity) {
        root = insertRec(root, id, name, price, quantity);
    }

    Item insertRec(Item root, int id, String name, double price, int quantity) {
        if (root == null) return new Item(id, name, price, quantity);
        if (id < root.id) root.left = insertRec(root.left, id, name, price, quantity);
        else if (id > root.id) root.right = insertRec(root.right, id, name, price, quantity);
        return root;
    }

    String getAllItemsJson() {
        List<Item> items = new ArrayList<>();
        inorder(root, items);
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            json.append("{\"id\":").append(item.id)
                .append(",\"name\":\"").append(item.name)
                .append("\",\"price\":").append(item.price)
                .append(",\"quantity\":").append(item.quantity).append("}");
            if (i < items.size() - 1) json.append(",");
        }
        json.append("]");
        return json.toString();
    }

    String getTreeJson() {
        StringBuilder json = new StringBuilder();
        json.append("{\"tree\":");
        json.append(getTreeNodeJson(root));
        json.append("}");
        return json.toString();
    }

    String getTreeNodeJson(Item node) {
        if (node == null) return "null";
        StringBuilder json = new StringBuilder();
        json.append("{\"id\":").append(node.id)
            .append(",\"name\":\"").append(node.name)
            .append("\",\"price\":").append(node.price)
            .append(",\"quantity\":").append(node.quantity)
            .append(",\"left\":").append(getTreeNodeJson(node.left))
            .append(",\"right\":").append(getTreeNodeJson(node.right))
            .append("}");
        return json.toString();
    }

    void inorder(Item root, List<Item> items) {
        if (root != null) {
            inorder(root.left, items);
            items.add(root);
            inorder(root.right, items);
        }
    }

    Item search(int id) {
        return searchRec(root, id);
    }

    Item searchRec(Item root, int id) {
        if (root == null || root.id == id) return root;
        if (id < root.id) return searchRec(root.left, id);
        return searchRec(root.right, id);
    }

    void delete(int id) {
        root = deleteRec(root, id);
    }

    Item deleteRec(Item root, int id) {
        if (root == null) return root;
        if (id < root.id) root.left = deleteRec(root.left, id);
        else if (id > root.id) root.right = deleteRec(root.right, id);
        else {
            if (root.left == null) return root.right;
            else if (root.right == null) return root.left;
            Item temp = minValueNode(root.right);
            root.id = temp.id;
            root.name = temp.name;
            root.price = temp.price;
            root.quantity = temp.quantity;
            root.right = deleteRec(root.right, temp.id);
        }
        return root;
    }

    Item minValueNode(Item root) {
        Item current = root;
        while (current.left != null) current = current.left;
        return current;
    }


}

public class SimpleInventory {
    static BST inventory = new BST();

    public static void main(String[] args) {
        try {
            inventory.insert(105, "Dell Laptop", 45000, 8);
            inventory.insert(102, "Wireless Mouse", 850, 45);
            inventory.insert(108, "USB Keyboard", 1500, 25);
            inventory.insert(110, "Monitor 24 inch", 12000, 15);
            inventory.insert(115, "Webcam HD", 3500, 20);
            inventory.insert(101, "HP Printer", 8500, 5);

            ServerSocket server = new ServerSocket(8080);
            System.out.println("Server started at http://localhost:8080");
            
            while (true) {
                try {
                    Socket client = server.accept();
                    new Thread(() -> handleRequest(client)).start();
                } catch (Exception e) {
                    System.out.println("Error: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.out.println("Server error: " + e.getMessage());
        }
    }

    static void handleRequest(Socket client) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            OutputStream out = client.getOutputStream();

            String requestLine = in.readLine();
            if (requestLine == null) {
                client.close();
                return;
            }

            if (requestLine.startsWith("GET / ")) {
                String line;
                while ((line = in.readLine()) != null && !line.isEmpty()) {}
                sendHTML(out);
            } else if (requestLine.startsWith("GET /api/tree")) {
                String line;
                while ((line = in.readLine()) != null && !line.isEmpty()) {}
                sendJSON(out, inventory.getTreeJson());
            } else if (requestLine.startsWith("GET /api/items")) {
                String line;
                while ((line = in.readLine()) != null && !line.isEmpty()) {}
                sendJSON(out, inventory.getAllItemsJson());
            } else if (requestLine.startsWith("POST /api/items")) {
                StringBuilder body = new StringBuilder();
                int contentLength = 0;
                String headerLine;
                while ((headerLine = in.readLine()) != null && !headerLine.isEmpty()) {
                    if (headerLine.startsWith("Content-Length:")) {
                        contentLength = Integer.parseInt(headerLine.split(":")[1].trim());
                    }
                }
                if (contentLength > 0) {
                    char[] buffer = new char[contentLength];
                    in.read(buffer, 0, contentLength);
                    body.append(buffer);
                }
                String formData = body.toString();
                String[] params = formData.split("&");
                int id = 0; String name = ""; double price = 0; int quantity = 0;
                for (String param : params) {
                    if (param.contains("=")) {
                        String[] kv = param.split("=", 2);
                        try {
                            if (kv[0].equals("id") && kv.length > 1) id = Integer.parseInt(kv[1]);
                            else if (kv[0].equals("name") && kv.length > 1) name = java.net.URLDecoder.decode(kv[1], "UTF-8");
                            else if (kv[0].equals("price") && kv.length > 1) price = Double.parseDouble(kv[1]);
                            else if (kv[0].equals("quantity") && kv.length > 1) quantity = Integer.parseInt(kv[1]);
                        } catch (Exception e) {}
                    }
                }
                inventory.insert(id, name, price, quantity);
                sendJSON(out, "{\"success\":true}");
            } else if (requestLine.startsWith("DELETE /api/items/")) {
                String line;
                while ((line = in.readLine()) != null && !line.isEmpty()) {}
                String[] parts = requestLine.split("/");
                if (parts.length > 3) {
                    int id = Integer.parseInt(parts[3].split(" ")[0]);
                    inventory.delete(id);
                    sendJSON(out, "{\"success\":true}");
                } else {
                    send404(out);
                }
            } else if (requestLine.startsWith("PUT /api/items/")) {
                StringBuilder body = new StringBuilder();
                int contentLength = 0;
                String headerLine;
                while ((headerLine = in.readLine()) != null && !headerLine.isEmpty()) {
                    if (headerLine.startsWith("Content-Length:")) {
                        contentLength = Integer.parseInt(headerLine.split(":")[1].trim());
                    }
                }
                if (contentLength > 0) {
                    char[] buffer = new char[contentLength];
                    in.read(buffer, 0, contentLength);
                    body.append(buffer);
                }
                String formData = body.toString();
                String[] params = formData.split("&");
                int id = 0; String name = ""; double price = 0; int quantity = 0;
                for (String param : params) {
                    if (param.contains("=")) {
                        String[] kv = param.split("=", 2);
                        try {
                            if (kv[0].equals("id") && kv.length > 1) id = Integer.parseInt(kv[1]);
                            else if (kv[0].equals("name") && kv.length > 1) name = java.net.URLDecoder.decode(kv[1], "UTF-8");
                            else if (kv[0].equals("price") && kv.length > 1) price = Double.parseDouble(kv[1]);
                            else if (kv[0].equals("quantity") && kv.length > 1) quantity = Integer.parseInt(kv[1]);
                        } catch (Exception e) {}
                    }
                }
                Item existing = inventory.search(id);
                if (existing != null) {
                    existing.name = name;
                    existing.price = price;
                    existing.quantity = quantity;
                } else {
                    inventory.insert(id, name, price, quantity);
                }
                sendJSON(out, "{\"success\":true}");
            } else if (requestLine.startsWith("GET /api/search/")) {
                String line;
                while ((line = in.readLine()) != null && !line.isEmpty()) {}
                String[] parts = requestLine.split("/");
                if (parts.length > 3) {
                    int id = Integer.parseInt(parts[3].split(" ")[0]);
                    Item item = inventory.search(id);
                    if (item != null) {
                        String json = "{\"id\":" + item.id + ",\"name\":\"" + item.name + "\",\"price\":" + item.price + ",\"quantity\":" + item.quantity + "}";
                        sendJSON(out, json);
                    } else {
                        sendJSON(out, "{\"error\":\"Item not found\"}");
                    }
                } else {
                    send404(out);
                }
            } else {
                send404(out);
            }

            client.close();
        } catch (Exception e) {
            System.out.println("Request error: " + e.getMessage());
            try {
                client.close();
            } catch (Exception ex) {}
        }
    }

    static void sendHTML(OutputStream out) throws IOException {
        String html = "<!DOCTYPE html><html><head><meta charset='UTF-8'><meta name='viewport' content='width=device-width, initial-scale=1'><title>Binary Tree Inventory System</title>" +
            "<style>" +
            "* {margin:0;padding:0;box-sizing:border-box;}" +
            "body {font-family:'Inter',-apple-system,BlinkMacSystemFont,'Segoe UI',sans-serif;background:#f0f4f9;min-height:100vh;padding:20px;color:#333;}" +
            ".navbar {background:#fff;border-bottom:1px solid #e1e4e8;padding:20px 0;text-align:center;margin-bottom:30px;box-shadow:0 1px 3px rgba(0,0,0,0.08);}" +
            ".navbar h1 {font-size:26px;font-weight:700;color:#1a202c;letter-spacing:-0.5px;}" +
            ".container {max-width:1600px;margin:0 auto;}" +
            ".tree-container {background:#fff;padding:25px;border-radius:12px;border:1px solid #e1e4e8;min-height:500px;display:flex;justify-content:center;align-items:center;}" +
            "canvas {max-width:100%;height:auto;}" +
            ".form-group {margin:18px 0;}" +
            ".form-group label {display:block;margin-bottom:8px;font-weight:600;color:#2d3748;font-size:14px;}" +
            ".form-group input {width:100%;padding:11px 13px;border:1px solid #cfd7e0;border-radius:8px;font-size:14px;transition:all 0.2s;background:#fff;}" +
            ".form-group input:focus {outline:none;border-color:#3182ce;box-shadow:0 0 0 3px rgba(49,130,206,0.1);}" +
            ".buttons {display:flex;gap:10px;margin:25px 0;flex-wrap:wrap;}" +
            ".btn {padding:11px 18px;border:none;border-radius:8px;cursor:pointer;font-weight:600;transition:all 0.2s;font-size:13px;border:1px solid transparent;}" +
            ".btn-add {background:#10b981;color:white;}" +
            ".btn-add:hover {background:#059669;transform:translateY(-1px);box-shadow:0 2px 8px rgba(16,185,129,0.2);}" +
            ".btn-update {background:#3b82f6;color:white;}" +
            ".btn-update:hover {background:#2563eb;transform:translateY(-1px);box-shadow:0 2px 8px rgba(59,130,246,0.2);}" +
            ".btn-delete {background:#f97316;color:white;}" +
            ".btn-delete:hover {background:#ea580c;transform:translateY(-1px);box-shadow:0 2px 8px rgba(249,115,22,0.2);}" +
            ".btn-search {background:#8b5cf6;color:white;}" +
            ".btn-search:hover {background:#7c3aed;transform:translateY(-1px);box-shadow:0 2px 8px rgba(139,92,246,0.2);}" +
            ".btn-refresh {background:#6b7280;color:white;}" +
            ".btn-refresh:hover {background:#4b5563;transform:translateY(-1px);box-shadow:0 2px 8px rgba(107,114,128,0.2);}" +
            ".table-container {overflow-x:auto;}" +
            "table {width:100%;border-collapse:collapse;margin:20px 0;}" +
            "th {background:#f9fafb;color:#374151;padding:14px;text-align:left;font-weight:600;border-bottom:1px solid #e5e7eb;font-size:13px;letter-spacing:0.3px;}" +
            "td {padding:13px 14px;border-bottom:1px solid #f3f4f6;font-size:14px;}" +
            "tr:hover {background:#f9fafb;}" +
            ".message {padding:14px 16px;margin:15px 0;border-radius:8px;display:none;animation:slideIn 0.3s;border-left:4px solid;font-size:14px;}" +
            ".success {background:#d1fae5;color:#065f46;border-left-color:#10b981;}" +
            ".error {background:#fee2e2;color:#7f1d1d;border-left-color:#ef4444;}" +
            ".message.show {display:block;}" +
            ".info-box {background:#eff6ff;border:1px solid #bfdbfe;border-radius:8px;padding:13px;margin:12px 0;font-size:13px;color:#1e40af;}" +
            ".stats {display:grid;grid-template-columns:repeat(auto-fit,minmax(140px,1fr));gap:15px;margin:18px 0;}" +
            ".stat-card {background:linear-gradient(135deg,#ef4444 0%,#dc2626 100%);color:white;padding:18px;border-radius:10px;box-shadow:0 2px 8px rgba(239,68,68,0.15);}" +
            ".stat-label {font-size:12px;opacity:0.85;margin-bottom:6px;font-weight:500;text-transform:uppercase;letter-spacing:0.5px;}" +
            ".stat-value {font-size:26px;font-weight:700;}" +
            "@keyframes slideIn {from {opacity:0;transform:translateY(10px);} to {opacity:1;transform:translateY(0);}}" +
            "h2 {color:#1f2937;font-size:18px;font-weight:700;margin-bottom:15px;letter-spacing:-0.3px;}" +
            "</style></head><body>" +
            "<div class='navbar'><h1>Inventory Management System</h1></div>" +
            "<div class='container'>" +
            "<div id='message' class='message'></div>" +
            "<div style='display:grid;grid-template-columns:1fr 1fr;gap:20px;margin-bottom:20px;'>" +
            "<div style='background:white;padding:20px;border-radius:10px;box-shadow:0 1px 3px rgba(0,0,0,0.08);'>" +
            "<h2>Items Inventory</h2>" +
            "<div class='stats' id='stats'></div>" +
            "<div class='table-container'>" +
            "<table id='itemsTable'>" +
            "<thead><tr><th>ID</th><th>Name</th><th>Price</th><th>Qty</th><th>Total</th></tr></thead>" +
            "<tbody></tbody>" +
            "</table>" +
            "</div>" +
            "</div>" +
            "<div style='background:white;padding:20px;border-radius:10px;box-shadow:0 1px 3px rgba(0,0,0,0.08);'>" +
            "<h2>Add or Modify Items</h2>" +
            "<p style='margin:10px 0;font-size:13px;color:#6b7280;'>To update: Search for an item, modify the values, and click Update</p>" +
            "<div class='form-group'><label>Item ID</label><input type='number' id='id' placeholder='101'></div>" +
            "<div class='form-group'><label>Item Name</label><input type='text' id='name' placeholder='Product name'></div>" +
            "<div class='form-group'><label>Price (₹)</label><input type='number' id='price' placeholder='0.00' step='0.01'></div>" +
            "<div class='form-group'><label>Quantity</label><input type='number' id='quantity' placeholder='0'></div>" +
            "<div class='buttons'>" +
            "<button class='btn btn-add' onclick='addItem()'>Add Item</button>" +
            "<button class='btn btn-update' onclick='updateItem()'>Update Item</button>" +
            "<button class='btn btn-search' onclick='searchItem()'>Search</button>" +
            "<button class='btn btn-delete' onclick='deleteItem()'>Delete</button>" +
            "<button class='btn btn-refresh' onclick='refreshAll()'>Refresh Data</button>" +
            "</div>" +
            "</div>" +
            "</div>" +
            "<div style='background:white;padding:20px;border-radius:10px;box-shadow:0 1px 3px rgba(0,0,0,0.08);'>" +
            "<h2>Tree Structure Visualization</h2>" +
            "<p style='margin:10px 0;color:#6b7280;font-size:13px;'>Binary search tree organization for efficient item management:</p>" +
            "<div class='tree-container'><canvas id='treeCanvas'></canvas></div>" +
            "<div class='info-box'>Items are organized by ID in a binary tree structure for fast searching and sorting</div>" +
            "</div>" +
            "</div>" +
            "<script>" +
            "let allItems=[];" +
            "let treeData=null;" +
            "function drawTree(){" +
            "fetch('/api/tree').then(r=>r.json()).then(data=>{" +
            "treeData=data.tree;" +
            "let canvas=document.getElementById('treeCanvas');" +
            "if(!canvas)return;" +
            "let ctx=canvas.getContext('2d');" +
            "canvas.width=Math.max(800,Math.min(window.innerWidth-100,1200));" +
            "canvas.height=600;" +
            "ctx.fillStyle='#f8f9fa';" +
            "ctx.fillRect(0,0,canvas.width,canvas.height);" +
            "ctx.fillStyle='#333';" +
            "ctx.font='14px Arial';" +
            "ctx.textAlign='center';" +
            "if(treeData===null){" +
            "ctx.fillStyle='#999';" +
            "ctx.font='16px Arial';" +
            "ctx.fillText('Tree is empty - Add items to see the structure',canvas.width/2,canvas.height/2);" +
            "}else{" +
            "try{drawNode(ctx,treeData,canvas.width/2,50,Math.min(canvas.width/4,180));}catch(e){console.error('Draw error:',e);}" +
            "}" +
            "}).catch(e=>{" +
            "console.error('Tree error:',e);" +
            "});" +
            "}" +
            "function drawNode(ctx,node,x,y,xOffset){" +
            "if(node===null)return;" +
            "if(node.left){drawLine(ctx,x,y,x-xOffset,y+90);drawNode(ctx,node.left,x-xOffset,y+90,xOffset/1.8);}" +
            "if(node.right){drawLine(ctx,x,y,x+xOffset,y+90);drawNode(ctx,node.right,x+xOffset,y+90,xOffset/1.8);}" +
            "drawCircle(ctx,x,y,node);" +
            "}" +
            "function drawLine(ctx,fromX,fromY,toX,toY){" +
            "ctx.strokeStyle='#999';" +
            "ctx.lineWidth=2;" +
            "ctx.beginPath();" +
            "ctx.moveTo(fromX,fromY+30);" +
            "ctx.lineTo(toX,toY-30);" +
            "ctx.stroke();" +
            "}" +
            "function drawCircle(ctx,x,y,node){" +
            "let radius=40;" +
            "let gradient=ctx.createLinearGradient(x-radius,y-radius,x+radius,y+radius);" +
            "gradient.addColorStop(0,'#ef4444');" +
            "gradient.addColorStop(1,'#dc2626');" +
            "ctx.fillStyle=gradient;" +
            "ctx.beginPath();" +
            "ctx.arc(x,y,radius,0,2*Math.PI);" +
            "ctx.fill();" +
            "ctx.strokeStyle='#fff';" +
            "ctx.lineWidth=3;" +
            "ctx.stroke();" +
            "ctx.fillStyle='white';" +
            "ctx.font='bold 16px Arial';" +
            "ctx.textAlign='center';" +
            "ctx.fillText(node.id,x,y-8);" +
            "ctx.font='11px Arial';" +
            "ctx.fillStyle='#f0f0f0';" +
            "let nameMax=node.name.length>12?node.name.substring(0,10)+'..':node.name;" +
            "ctx.fillText(nameMax,x,y+12);" +
            "}" +
            "function loadItems(){" +
            "fetch('/api/items').then(r=>r.json()).then(items=>{" +
            "allItems=items;" +
            "displayItems(items);" +
            "updateStats(items);" +
            "}).catch(e=>showMessage('Error loading items','error'));" +
            "}" +
            "function updateStats(items){" +
            "let totalItems=items.length;" +
            "let totalValue=items.reduce((sum,item)=>sum+item.price*item.quantity,0);" +
            "let totalQty=items.reduce((sum,item)=>sum+item.quantity,0);" +
            "let html=`<div class='stat-card'><div class='stat-label'>Total Items</div><div class='stat-value'>${totalItems}</div></div>` +" +
            "`<div class='stat-card'><div class='stat-label'>Total Quantity</div><div class='stat-value'>${totalQty}</div></div>` +" +
            "`<div class='stat-card'><div class='stat-label'>Total Value</div><div class='stat-value'>₹${totalValue.toLocaleString('en-IN',{maximumFractionDigits:0})}</div></div>`;" +
            "document.getElementById('stats').innerHTML=html;" +
            "}" +
            "function displayItems(items){" +
            "let html='';" +
            "items.forEach(item=>{" +
            "let total=(item.price*item.quantity).toFixed(2);" +
            "html+=`<tr><td>${item.id}</td><td>${item.name}</td><td>₹${item.price.toLocaleString('en-IN')}</td><td>${item.quantity}</td><td>₹${parseFloat(total).toLocaleString('en-IN')}</td></tr>`;" +
            "});" +
            "document.querySelector('#itemsTable tbody').innerHTML=html;" +
            "}" +
            "function addItem(){" +
            "let id=document.getElementById('id').value;" +
            "let name=document.getElementById('name').value;" +
            "let price=document.getElementById('price').value;" +
            "let quantity=document.getElementById('quantity').value;" +
            "if(!id||!name||!price||!quantity){" +
            "showMessage('Please fill all fields','error');return;}" +
            "fetch('/api/items',{method:'POST',headers:{'Content-Type':'application/x-www-form-urlencoded'},body:`id=${id}&name=${encodeURIComponent(name)}&price=${price}&quantity=${quantity}`})" +
            ".then(r=>r.json()).then(()=>{" +
            "showMessage('Item added successfully','success');" +
            "clearForm();loadItems();drawTree();" +
            "}).catch(e=>showMessage('Failed to add item','error'));" +
            "}" +
            "function searchItem(){" +
            "let searchId=prompt('Enter Item ID to search:');" +
            "if(!searchId){return;}" +
            "fetch('/api/search/'+searchId).then(r=>r.json()).then(item=>{" +
            "if(item.error){" +
            "showMessage('Item ID '+searchId+' not found','error');" +
            "}else{" +
            "document.getElementById('id').value=item.id;" +
            "document.getElementById('name').value=item.name;" +
            "document.getElementById('price').value=item.price;" +
            "document.getElementById('quantity').value=item.quantity;" +
            "showMessage('Item '+item.id+' loaded - modify and click Update','success');" +
            "}" +
            "}).catch(e=>{console.error(e);showMessage('Search failed','error');});" +
            "}" +
            "function deleteItem(){" +
            "let id=prompt('Enter Item ID to delete:');" +
            "if(id&&confirm(`Delete item ${id}?`)){" +
            "fetch(`/api/items/${id}`,{method:'DELETE'}).then(r=>r.json()).then(()=>{" +
            "showMessage('Item deleted successfully','success');loadItems();drawTree();" +
            "}).catch(e=>showMessage('Failed to delete item','error'));" +
            "}" +
            "}" +
            "function updateItem(){" +
            "let id=document.getElementById('id').value;" +
            "let name=document.getElementById('name').value;" +
            "let price=document.getElementById('price').value;" +
            "let quantity=document.getElementById('quantity').value;" +
            "if(!id){" +
            "showMessage('Enter ID or search for an item to update','error');return;}" +
            "if(!name||!price||!quantity){" +
            "showMessage('Please fill name, price and quantity','error');return;}" +
            "fetch(`/api/items/${id}`,{method:'PUT',headers:{'Content-Type':'application/x-www-form-urlencoded'},body:`id=${id}&name=${encodeURIComponent(name)}&price=${price}&quantity=${quantity}`})" +
            ".then(r=>r.json()).then(()=>{" +
            "showMessage('Item updated successfully','success');clearForm();loadItems();drawTree();" +
            "}).catch(e=>showMessage('Failed to update item','error'));" +
            "}" +
            "function clearForm(){" +
            "document.getElementById('id').value='';" +
            "document.getElementById('name').value='';" +
            "document.getElementById('price').value='';" +
            "document.getElementById('quantity').value='';" +
            "}" +
            "function showMessage(msg,type){" +
            "let div=document.getElementById('message');" +
            "div.innerHTML=msg;" +
            "div.className='message '+type+' show';" +
            "setTimeout(()=>div.classList.remove('show'),4000);" +
            "}" +
            "function refreshAll(){" +
            "loadItems();drawTree();showMessage('Data refreshed','success');" +
            "}" +
            "window.addEventListener('load',()=>{" +
            "loadItems();" +
            "setTimeout(()=>drawTree(),500);" +
            "});" +
            "</script>" +
            "</body></html>";

        String response = "HTTP/1.1 200 OK\r\n" +
                         "Content-Type: text/html; charset=UTF-8\r\n" +
                         "Content-Length: " + html.getBytes("UTF-8").length + "\r\n" +
                         "\r\n" + html;
        
        out.write(response.getBytes());
        out.flush();
    }

    static void sendJSON(OutputStream out, String json) throws IOException {
        String response = "HTTP/1.1 200 OK\r\n" +
                         "Content-Type: application/json\r\n" +
                         "Content-Length: " + json.length() + "\r\n" +
                         "\r\n" + json;
        
        out.write(response.getBytes());
        out.flush();
    }

    static void send404(OutputStream out) throws IOException {
        String html = "<h1>404 Not Found</h1>";
        String response = "HTTP/1.1 404 Not Found\r\n" +
                         "Content-Type: text/html\r\n" +
                         "Content-Length: " + html.length() + "\r\n" +
                         "\r\n" + html;
        
        out.write(response.getBytes());
        out.flush();
    }
}