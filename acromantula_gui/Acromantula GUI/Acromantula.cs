using System;
using System.Windows.Forms;
using Acromantula_GUI.CodeView;
using WeifenLuo.WinFormsUI.Docking;

namespace Acromantula_GUI
{
    public partial class Acromantula : Form
    {

        private DockPanel DockPanel { get; set; } = new DockPanel();

        public Acromantula()
        {
            InitializeComponent();

            DockPanel.Dock = DockStyle.Fill;
            Controls.Add(DockPanel);
            DockPanel.BringToFront();
        }

        private void codeToolStripMenuItem_Click(object sender, EventArgs e)
        {
            var mainDock = new DockContent
            {
                ShowHint = DockState.Document,
                TabText = "Untitled Codeview"
            };

            mainDock.Controls.Add(new CodeViewMain().Scintilla);
            mainDock.Show(DockPanel);
        }

        private void compareToolStripMenuItem_Click(object sender, EventArgs e)
        {
            new CodeViewComparer {MdiParent = this}.Show();
        }
    }
}