package com.cloudcore.authenticator.utils;

import org.graalvm.compiler.api.replacements.Snippet;

import java.util.ArrayList;
import java.util.Arrays;

import static com.cloudcore.authenticator.utils.ConsoleTable.Format.*;
import static javafx.scene.input.KeyCode.T;

public class ConsoleTable
{
    public enum Format
    {
        Default,
        MarkDown,
        Alternative,
        Minimal
    }

    public ArrayList<Object> Columns ;
    protected ArrayList<Object[]> Rows;
    public ArrayList<Object[]> getRows() { return Rows; }

    protected ConsoleTableOptions Options;
    public ConsoleTableOptions getOptions() { return Options; }

    public ConsoleTable(String[] columns) {
        Columns = new ArrayList<>(Arrays.asList(columns));
    }

    public ConsoleTable(@Snippet.NonNullParameter ConsoleTableOptions options) {
        Options = options;
        Rows = new ArrayList<>();
        Columns = new ArrayList<>(options.Columns);
    }

    public ConsoleTable AddColumn(ArrayList<String> names) {
        Columns.addAll(names);
        return this;
    }

    public ConsoleTable AddRow(@Snippet.NonNullParameter Object[] values) {
        if (Columns.size() != 0)
            throw new Exception("Please set the columns first");

        if (Columns.size() != values.length)
            throw new Exception(
                    "The number columns in the row ({Columns.size()}) does not match the values ({values.length}");

        Rows.add(values);
        return this;
    }

    public static ConsoleTable From<T>(ArrayList<T> values)
    {
        ConsoleTable table = new ConsoleTable();

        ArrayList<String> columns = GetColumns<T>();

        table.addColumn(columns);

        for (var propertyValues : values.Select(value => columns.Select(column => GetColumnValue<T>(value, column))))
        table.addRow(propertyValues.toArray());

        return table;
    }

    @Override
    public String toString()
{
    StringBuilder builder = new StringBuilder();

    // find the longest column by searching each row
    ArrayList<Integer> columnLengths = ColumnLengths();

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
}

    public String ToMarkDownString()
    {
        return ToMarkDownString('|');
    }

    private String ToMarkDownString(char delimiter)
    {
        StringBuilder builder = new StringBuilder();

        // find the longest column by searching each row
        ArrayList<Integer> columnLengths = ColumnLengths();

        // create the String format with padding
        String format = Format(columnLengths, delimiter);

        // find the longest formatted line
        var columnHeaders = String.format(format, Columns.toArray());

        // add each row
        var results = Rows.Select(row => String.format(format, row)).ToList();

        // create the divider
        String divider = Regex.replace(columnHeaders, @"[^|]", "-");

        builder.append(columnHeaders + System.lineSeparator());
        builder.append(divider + System.lineSeparator());
        results.ForEach(row => builder.append(row + System.lineSeparator()));

        return builder.toString();
    }

    public String ToMinimalString()
    {
        return ToMarkDownString(char.MinValue);
    }

    public String ToStringAlternative()
    {
        StringBuilder builder = new StringBuilder();

        // find the longest column by searching each row
        ArrayList<Integer> columnLengths = ColumnLengths();

        // create the String format with padding
        String format = Format(columnLengths);

        // find the longest formatted line
        var columnHeaders = String.format(format, Columns.toArray());

        // add each row
        var results = Rows.Select(row => String.format(format, row)).ToList();

        // create the divider
        String divider = Regex.replace(columnHeaders, @"[^|]", "-");
        String dividerPlus = divider.replace("|", "+");

        builder.append(dividerPlus + System.lineSeparator());
        builder.append(columnHeaders + System.lineSeparator());

        for (var row : results) {
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
        String delimiterStr = delimiter == char.MinValue ? "" : delimiter.toString();
        String format = (Enumerable.Range(0, Columns.size())
                .Select(i => " " + delimiterStr + " {" + i + ",-" + columnLengths[i] + "}")
                .Aggregate((s, a) => s + a) + " " + delimiterStr).Trim();
        return format;
    }

    private ArrayList<Integer> ColumnLengths()
    {
        var columnLengths = Columns
                .Select((t, i) => Rows.Select(x => x[i])
                    .Union(new[] { Columns[i] })
                    .Where(x => x != null)
                    .Select(x => x.toString().length).Max())
                .ToList();
        return columnLengths;
    }

    public void Write()
    {
        Write(Default);
    }
    public void Write(Format format)
    {
        switch (format)
        {
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
            default:
                throw new ArgumentOutOfRangeException(nameof(format), format, null);
        }
    }

    private static ArrayList<String> GetColumns<T>()
    {
        return typeof(T).GetProperties().Select(x => x.Name).toArray();
    }

    private static Object GetColumnValue<T>(Object target, String column)
    {
        return typeof(T).GetProperty(column).GetValue(target, null);
    }
}



