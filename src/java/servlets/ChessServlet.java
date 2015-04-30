/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servlets;

import credentials.Credentials;
import static credentials.Credentials.getConnection;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Dylan Huculak - c0630163
 */
@WebServlet(name = "ChessServlet", urlPatterns = {"/chess_games"})
public class ChessServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
            response.setHeader("Content-Type", "text/plain-text");
            Connection conn = getConnection();
            try (PrintWriter out = response.getWriter()) {
                if (!request.getParameterNames().hasMoreElements()){
                    out.println(getResults("SELECT * FROM chess_games"));
                } else {
                    String id = request.getParameter("id");
                    out.println(getResults("SELECT * FROM chess_games WHERE gameId = ?", id));  
                }
                conn.close();
            } catch (SQLException ex){
                 Logger.getLogger(ChessServlet.class.getName()).log(Level.SEVERE, null, ex);
            }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Connection conn = getConnection();
        Set<String> keySet = request.getParameterMap().keySet();
        try (PrintWriter out = response.getWriter()) {
            
            //DEBUGGING OUTPUTS
            out.println("request: " + request);
            out.println("reqAttribs: " + request.getAttributeNames());
            out.println("keySet: " + keySet.toString());
            
            if (keySet.contains("fen")) {
                String fen = request.getParameter("fen");
                PreparedStatement pstmt = conn.prepareStatement("INSERT INTO chess_games ('fen') VALUES ('"
                    + fen +"')");
                try {
                    pstmt.executeUpdate();
                    out.println("http://localhost:8080/JavaTermChess/chess_games/" + request.getParameter("id"));
                } catch (SQLException ex) {
                    Logger.getLogger(ChessServlet.class.getName()).log(Level.SEVERE, null, ex);
                    out.println("Error: problem inserting values.");
                    response.setStatus(500);
                }
            } else {
                out.println("Error: Cannot post. Insufficient data.");
                response.setStatus(500);
            }
            conn.close();
        } catch (SQLException ex) {
            Logger.getLogger(ChessServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) 
            throws IOException, ServletException {
        Set<String> keySet = request.getParameterMap().keySet();
        Connection conn = getConnection();
        try (PrintWriter out = response.getWriter()) {
            if (keySet.contains("gameId") && keySet.contains("fen") 
                    && keySet.contains("description") 
                    && keySet.contains("quantity")) {
                String gameid = request.getParameter("id");
                String fen = request.getParameter("fen");
                String description = request.getParameter("description");
                String quantity = request.getParameter("quantity");
                PreparedStatement pstmt = conn.prepareStatement("UPDATE chess_games SET fen='" 
                    + fen + "'" + "' WHERE gameId = '"
                    + gameid + "'");
                try {
                    pstmt.executeUpdate();
                    out.println("http://localhost:8080/JavaTermChess/chess_games/" 
                            + request.getParameter("id"));
                } catch (SQLException ex) {
                    Logger.getLogger(ChessServlet.class.getName()).log(Level.SEVERE, null, ex);
                    out.println("Error: cannot update values.");
                    response.setStatus(500);
                }
            } else {
                out.println("Error: insufficient parameters for update.");
                response.setStatus(500);
            }
            conn.close();
        } catch (SQLException ex) {
            Logger.getLogger(ChessServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    private String getResults(String query, String... params){
        StringBuilder sb = new StringBuilder();
        Connection conn = Credentials.getConnection();
        try {
            PreparedStatement pstmt = conn.prepareStatement(query);
            for (int i = 1; i <= params.length; i++) {
                pstmt.setString(i, params[i - 1]);
            }
            ResultSet rs = pstmt.executeQuery();
            sb.append("[\r\n");
            while (rs.next()) {
                sb.append(String.format("\t{\r\n\t\t\"gameId\" : %s,\r\n"
                        + "\t\t\"fen\" : \"%s\",\r\n", 
                        rs.getInt("gameId"), 
                        rs.getString("fen")));
            }
            sb.setLength(Math.max(sb.length() - 3, 0));
            sb.append("\r\n]");
            conn.close();
        } catch (SQLException ex) {
            Logger.getLogger(ChessServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
        return sb.toString();
    }
    
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        Set<String> keySet = request.getParameterMap().keySet();
        Connection conn = getConnection();
        try (PrintWriter out = response.getWriter()) {
            if (keySet.contains("id")) {
                PreparedStatement pstmt = conn.prepareStatement(
                        "DELETE FROM chess_games WHERE gameId = " 
                        + request.getParameter("gameId"));
                try {
                    pstmt.executeUpdate();
                    out.println("");
                } catch (SQLException ex) {
                    Logger.getLogger(ChessServlet.class.getName()).log(Level.SEVERE, null, ex);
                    out.println("Error deleting entry");
                    response.setStatus(500);
                }
            } else {
                out.println("No data to delete");
                response.setStatus(500);
            }
            conn.close();
        }
        catch (SQLException ex) {
            Logger.getLogger(ChessServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
            
}
