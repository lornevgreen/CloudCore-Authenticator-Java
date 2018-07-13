package com.cloudcore.authenticator.utils;

import java.util.ArrayList;
import java.util.Arrays;

import static com.cloudcore.authenticator.utils.ConsoleTable.Format.Default;

public class ConsoleTable {
    public enum Format {
        Default,
        MarkDown,
        Alternative,
        Minimal
    }

    public ArrayList<Object> Columns;
    protected ArrayList<Object[]> Rows;

    public ArrayList<Object[]> getRows() {
        return Rows;
    }

    protected ConsoleTableOptions Options;

    public ConsoleTableOptions getOptions() {
        return Options;
    }

    public ConsoleTable() {
    }

    public ConsoleTable(String[] columns) {
        Columns = new ArrayList<>(Arrays.asList(columns));
    }

    public ConsoleTable(ConsoleTableOptions options) {
        Options = options;
        Rows = new ArrayList<>();
        Columns = new ArrayList<>(options.Columns);
    }

    public ConsoleTable AddColumn(ArrayList<String> names) {
        Columns.addAll(names);
        return this;
    }

    public ConsoleTable AddRow(Object[] values) {
        if (Columns.size() != 0) {
            System.out.println("Please set the columns first");
            System.exit(-1);
            return null;
        }

        if (Columns.size() != values.length) {
            System.out.println("The number columns in the row ({Columns.size()}) does not match the values ({values.length}");
            System.exit(-1);
            return null;
        }

        Rows.add(values);
        return this;
    }

    /* TODO: finish
    @Override
    public String toString()  {
        StringBuilder builder = new StringBuilder();

        // find the longest column by searching each row
        ArrayList<Integer> columnLengths = ColumnLengths();

        // create the String format with padding
        String format = Format(columnLengths);

        // find the longest formatted line
        String columnHeaders = String.format(format, Columns.toArray());

        // add each row
        String[] results = new String[Rows.size()];
        for (int i = 0; i < Rows.size(); i++) {
            results[i] = String.format(format, Rows.get(i));
        }

    // create the String format with padding
    var format = Enumerable.Range(0, Columns.size())
            .Select(i => " | {" + i + ",-" + columnLengths[i] + "}")
                .Aggregate((s, a) => s + a) + " |";

    // find the longest formatted line
    int maxRowLength = Math.max(0, Rows.size() != 0 ? Rows.Max(row => String.format(format, row).length) : 0);
    var columnHeaders = String.format(format, Columns.toArray());

    // longest line is greater of formatted columnHeader and longest row
    int longestLine = Math.max(maxRowLength, columnHeaders.length);

    // add each row
    var results = Rows.Select(row => String.format(format, row)).ToList();

    // create the divider
    String divider = " " + String.Join("", Enumerable.Repeat("-", longestLine - 1)) + " ";

    builder.append(divider + System.lineSeparator());
    builder.append(columnHeaders + System.lineSeparator());

    for (String row : results)
    {
        builder.append(divider + System.lineSeparator());
        builder.append(row + System.lineSeparator());
    }

    builder.append(divider + System.lineSeparator());

    if (Options.EnableCount)
    {
        builder.append("" + System.lineSeparator());
        builder.AppendFormat(" Count: {0}", Rows.size());
    }

    return builder.toString();
}*/

    public String ToMarkDownString() {
        return ToMarkDownString('|');
    }

    private String ToMarkDownString(char delimiter) {
        StringBuilder builder = new StringBuilder();

        // find the longest column by searching each row
        ArrayList<Integer> columnLengths = ColumnLengths();

        // create the String format with padding
        String format = Format(columnLengths);

        // find the longest formatted line
        String columnHeaders = String.format(format, Columns.toArray());

        // add each row
        String[] results = new String[Rows.size()];
        for (int i = 0; i < Rows.size(); i++) {
            results[i] = String.format(format, Rows.get(i));
        }

        // create the divider
        String divider = columnHeaders.replaceAll("[^|]", "-");

        builder.append(columnHeaders + System.lineSeparator());
        builder.append(divider + System.lineSeparator());
        for (String result : results) builder.append(result + System.lineSeparator());

        return builder.toString();
    }

    public String ToMinimalString() {
        return ToMarkDownString(Character.MIN_VALUE);
    }

    public String ToStringAlternative() {
        StringBuilder builder = new StringBuilder();

        // find the longest column by searching each row
        ArrayList<Integer> columnLengths = ColumnLengths();

        // create the String format with padding
        String format = Format(columnLengths);

        // find the longest formatted line
        String columnHeaders = String.format(format, Columns.toArray());

        // add each row
        String[] results = new String[Rows.size()];
        for (int i = 0; i < Rows.size(); i++) {
            results[i] = String.format(format, Rows.get(i));
        }

        // create the divider
        String divider = columnHeaders.replaceAll("\\[\\^\\|]", "-");
        String dividerPlus = divider.replace("|", "+");

        builder.append(dividerPlus + System.lineSeparator());
        builder.append(columnHeaders + System.lineSeparator());

        for (String row : results) {
            builder.append(dividerPlus + System.lineSeparator());
            builder.append(row + System.lineSeparator());
        }
        builder.append(dividerPlus + System.lineSeparator());

        return builder.toString();
    }


    private String Format(ArrayList<Integer> columnLengths) {
        return Format(columnLengths, '|');
    }

    private String Format(ArrayList<Integer> columnLengths, char delimiter) {
        return "NOT YET IMPLEMENTED";
        /*String delimiterStr = delimiter == char.MinValue ? "" : Character.toString(delimiter);
        String format = (Enumerable.Range(0, Columns.size())
                .Select(i => " " + delimiterStr + " {" + i + ",-" + columnLengths[i] + "}")
                .Aggregate((s, a) => s + a) + " " + delimiterStr).Trim();
        return format;*/
    }

    private ArrayList<Integer> ColumnLengths() {
        /*var columnLengths = Columns
                .Select((t, i) => Rows.Select(x => x[i])
                    .Union(new[] { Columns[i] })
                    .Where(x => x != null)
                    .Select(x => x.toString().length).Max())
                .ToList();
        return columnLengths;*/
        return new ArrayList<>();
    }

    public void Write() {
        Write(Default);
    }

    public void Write(Format format) {
        switch (format) {
            default:
            case Default:
                System.out.println(toString());
                break;
            case MarkDown:
                System.out.println(ToMarkDownString());
                break;
            case Alternative:
                System.out.println(ToStringAlternative());
                break;
            case Minimal:
                System.out.println(ToMinimalString());
                break;
        }
    }
}



