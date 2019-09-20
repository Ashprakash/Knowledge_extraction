package WSC;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Arpit Sharma
 * @date Jun 11, 2017
 *
 */
public class DiscParsedSent {
	@Setter (AccessLevel.PUBLIC) @Getter (AccessLevel.PUBLIC) private String[] part1 = null;
	@Setter (AccessLevel.PUBLIC) @Getter (AccessLevel.PUBLIC) private int part1Indx = -999;
	@Setter (AccessLevel.PUBLIC) @Getter (AccessLevel.PUBLIC) private String[] part2 = null;
	@Setter (AccessLevel.PUBLIC) @Getter (AccessLevel.PUBLIC) private int part2Indx = -999;
	@Setter (AccessLevel.PUBLIC) @Getter (AccessLevel.PUBLIC) private String discConn = null;
	@Setter (AccessLevel.PUBLIC) @Getter (AccessLevel.PUBLIC) private String connType = null;	
}

