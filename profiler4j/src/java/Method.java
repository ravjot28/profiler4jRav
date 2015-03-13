import java.util.List;

public class Method {
	private List<Node> nodes;
	private int maxHeight;
	private int minCPD;
	private int induced;
	private Method commonParent;
	private boolean isSubsumed;
	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Node> getNodes() {
		return nodes;
	}

	public void setNodes(List<Node> nodes) {
		this.nodes = nodes;
	}

	public int getMaxHeight() {
		return maxHeight;
	}

	public void setMaxHeight(int maxHeight) {
		this.maxHeight = maxHeight;
	}

	public int getMinCPD() {
		return minCPD;
	}

	public void setMinCPD(int minCPD) {
		this.minCPD = minCPD;
	}

	public int getInduced() {
		return induced;
	}

	public void setInduced(int induced) {
		this.induced = induced;
	}

	public Method getCommonParent() {
		return commonParent;
	}

	public void setCommonParent(Method commonParent) {
		this.commonParent = commonParent;
	}

	public boolean isSubsumed() {
		return isSubsumed;
	}

	public void setSubsumed(boolean isSubsumed) {
		this.isSubsumed = isSubsumed;
	}

}
