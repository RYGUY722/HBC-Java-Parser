package marist.hbcModel;

import java.util.ArrayList;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

public class MethodVisitor extends VoidVisitorAdapter<Void> {
    ArrayList<String> mnames=new ArrayList<String>();
    @Override
    public void visit(MethodDeclaration n, Void arg) {
        mnames.add(n.getName().toString());
        super.visit(n, arg);
    }
    public ArrayList<String> getMethods(){
    	return mnames;
    }
}
