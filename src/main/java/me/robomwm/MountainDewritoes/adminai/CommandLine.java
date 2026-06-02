package me.robomwm.MountainDewritoes.adminai;

import java.util.ArrayList;
import java.util.List;

class CommandLine
{
    static List<String> split(String command)
    {
        ArrayList<String> tokens = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean singleQuoted = false;
        boolean doubleQuoted = false;
        boolean escaped = false;

        for (int i = 0; i < command.length(); i++)
        {
            char c = command.charAt(i);
            if (escaped)
            {
                current.append(c);
                escaped = false;
            }
            else if (c == '\\' && !singleQuoted)
                escaped = true;
            else if (c == '\'' && !doubleQuoted)
                singleQuoted = !singleQuoted;
            else if (c == '"' && !singleQuoted)
                doubleQuoted = !doubleQuoted;
            else if (Character.isWhitespace(c) && !singleQuoted && !doubleQuoted)
            {
                if (!current.isEmpty())
                {
                    tokens.add(current.toString());
                    current.setLength(0);
                }
            }
            else
                current.append(c);
        }
        if (!current.isEmpty())
            tokens.add(current.toString());
        return tokens;
    }
}
