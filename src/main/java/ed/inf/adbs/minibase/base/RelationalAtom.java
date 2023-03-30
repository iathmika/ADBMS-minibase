package ed.inf.adbs.minibase.base;

import ed.inf.adbs.minibase.Utils;

import java.util.List;

public class RelationalAtom extends Atom {
    private String name;

    private List<Term> terms;

    public RelationalAtom(String name, List<Term> terms) {
        this.name = name;
        this.terms = terms;
    }

    public String getName() {
        return name;
    }

    public List<Term> getTerms() {
        return terms;
    }

    @Override
    public String toString() {
        return name + "(" + Utils.join(terms, ", ") + ")";
    }


    @Override
    public boolean equals(Object object)
    {
        boolean isEqual= false;

        if (object != null && object instanceof RelationalAtom)
        {
            isEqual = true;
            RelationalAtom ra = new RelationalAtom(((RelationalAtom)object).getName(),((RelationalAtom)object).getTerms());
            String newname = ra.getName();
            String name = this.name;
            List<Term> terms = this.terms;
            List<Term> newterms = ra.getTerms();// System.out.println("ra: "+ra);
            // System.out.println("old terms: "+terms);
            //  System.out.println("newterms: "+newterms);
            for(int i=0;i<terms.size();i++){
                if ((terms.get(i) instanceof Variable) && (newterms.get(i) instanceof Variable)){
                    if(!((terms.get(i)).toString().equals((newterms.get(i)).toString()))){
                        //System.out.println("Terms is an instance of Variable");
                        //  System.out.println("Not equal: "+ ((Variable) terms.get(i)).getName() + " and "+ ((Variable) newterms.get(i)).getName());
                        isEqual = false;
                        break;
                    }
                }
                else if ((newterms.get(i) instanceof StringConstant) && (terms.get(i) instanceof StringConstant)){
                    if(!(((StringConstant)terms.get(i)).getValue().equals(((StringConstant)newterms.get(i)).getValue()))){
                        isEqual = false;
                        break;
                    }

                }
                else{
                    if((newterms.get(i) instanceof IntegerConstant) && (terms.get(i) instanceof IntegerConstant)) {
                        if (!(((IntegerConstant) terms.get(i)).getValue() == (((IntegerConstant) newterms.get(i)).getValue()))) {
                            isEqual = false;
                            break;
                        }
                    }
                    else{
                        isEqual = false;
                        break;
                    }
                }
            }


            isEqual = (isEqual && (name.equals(newname)));
        }

        return isEqual;
    }


}
